package ec.edu.logistpulse.service;
import ec.edu.logistpulse.chain.*;
import ec.edu.logistpulse.domain.*;
import ec.edu.logistpulse.security.ActorDirectory;
import java.time.*;
import java.util.Objects;
/** Independent receiver for adjustments and inter-warehouse stock transfers. */
public final class StockMutationService {
    private final Inventory inventory;
    private final ValidationHandler chain;
    private final Clock clock;
    private final IdSequence ids;
    private final ActorDirectory actors;
    public StockMutationService(Inventory inventory, ValidationHandler chain,Clock clock,
                                IdSequence ids,ActorDirectory actors) {
        this.inventory=Objects.requireNonNull(inventory); this.chain=Objects.requireNonNull(chain);
        this.clock=Objects.requireNonNull(clock); this.ids=Objects.requireNonNull(ids);
        this.actors=Objects.requireNonNull(actors);
    }
    public OperationResult quarantine(String actor,String lotId) {
        ValidationResult v=chain.handle(new ValidationContext(OperationRequest.quarantine(actor,lotId),inventory,LocalDate.now(clock)));
        if (!v.valid()) return OperationResult.fail(v.rejectedBy(),v.reason());
        inventory.lot(lotId).quarantineLot();
        return OperationResult.ok(lotId,"Lot moved to quarantine");
    }
    public OperationResult reinstate(String actor,String lotId) {
        ValidationResult v=chain.handle(new ValidationContext(OperationRequest.reinstate(actor,lotId),inventory,LocalDate.now(clock)));
        if (!v.valid()) return OperationResult.fail(v.rejectedBy(),v.reason());
        inventory.lot(lotId).reinstate();
        return OperationResult.ok(lotId,"Lot reinstated after quality inspection");
    }
    public OperationResult store(String actor,String lotId) {
        ValidationResult v=chain.handle(new ValidationContext(OperationRequest.store(actor,lotId),inventory,LocalDate.now(clock)));
        if (!v.valid()) return OperationResult.fail(v.rejectedBy(),v.reason());
        inventory.lot(lotId).store();
        return OperationResult.ok(lotId,"Received lot put away in warehouse");
    }
    public OperationResult unstore(String actor,String lotId) {
        ValidationResult v=chain.handle(new ValidationContext(OperationRequest.unstore(actor,lotId),inventory,LocalDate.now(clock)));
        if (!v.valid()) return OperationResult.fail(v.rejectedBy(),v.reason());
        inventory.lot(lotId).undoStore();
        return OperationResult.ok(lotId,"Put-away safely reverted to RECEIVED");
    }
    public OperationResult adjust(String actor,String lotId,int delta) {
        OperationRequest request=OperationRequest.adjust(actor,lotId,delta);
        ValidationResult v=chain.handle(new ValidationContext(request,inventory,LocalDate.now(clock)));
        if (!v.valid()) return OperationResult.fail(v.rejectedBy(),v.reason());
        inventory.lot(lotId).adjust(delta);
        return OperationResult.ok(lotId,"Adjusted lot "+lotId+" by "+delta+" unit(s)");
    }
    public OperationResult transfer(String actor,String lotId,String targetWh,int quantity) {
        OperationRequest request=OperationRequest.transfer(actor,lotId,targetWh,quantity);
        ValidationResult v=chain.handle(new ValidationContext(request,inventory,LocalDate.now(clock)));
        if (!v.valid()) return OperationResult.fail(v.rejectedBy(),v.reason());
        StockLot origin=inventory.lot(lotId);
        String newId=ids.next("LOT-T");
        // A transfer conserves product, expiry and quantity; stock is not created.
        StockLot destination=new StockLot(newId,origin.sku(),targetWh,quantity,
                LocalDate.now(clock),origin.expiresAt(),false);
        origin.adjust(-quantity);
        inventory.addLot(destination);
        return OperationResult.ok(newId,"Transferred "+quantity+" unit(s) from "+lotId+" to "+newId);
    }
    public OperationResult undoTransfer(String actor,String sourceId,String targetLotId,int quantity) {
        if (actors.roleOf(actor)==null || !actors.isSupervisor(actor))
            return OperationResult.fail("AUTHORIZATION","Supervisor role required");
        StockLot source=inventory.lot(sourceId),target=inventory.lot(targetLotId);
        boolean usedInReservations=inventory.reservations().stream()
                .anyMatch(r->r.allocations().stream().anyMatch(a->a.lotId().equals(targetLotId)));
        if (source==null || target==null || target.reserved()!=0 || target.onHand()!=quantity
                || usedInReservations || !source.sku().equals(target.sku())
                || source.warehouseId().equals(target.warehouseId()))
            return OperationResult.fail("UNDO","Transferred lot was modified/reserved: safe undo impossible");
        Warehouse wh=inventory.warehouse(source.warehouseId());
        if ((long)inventory.warehouseOnHand(wh.id())+quantity>wh.storageCapacity())
            return OperationResult.fail("CAPACITY","Not enough space in original warehouse to undo transfer");
        if ((long)source.onHand()+quantity>Integer.MAX_VALUE)
            return OperationResult.fail("STOCK","Overflow restoring source lot");
        inventory.removeLot(targetLotId);
        source.adjust(quantity);
        return OperationResult.ok(sourceId,"Transfer undone; restored "+quantity+" unit(s) to "+sourceId);
    }
}
