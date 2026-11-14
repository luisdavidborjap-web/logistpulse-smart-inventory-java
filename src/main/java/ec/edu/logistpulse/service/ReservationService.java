package ec.edu.logistpulse.service;
import ec.edu.logistpulse.chain.*;
import ec.edu.logistpulse.domain.*;
import ec.edu.logistpulse.security.ActorDirectory;
import ec.edu.logistpulse.strategy.picking.PickingContext;
import java.time.*;
import java.util.*;
/** Focuses only on allocating, releasing and restoring reservations. */
public final class ReservationService {
    private final Inventory inventory;
    private final PickingContext picking;
    private final ValidationHandler chain;
    private final Clock clock;
    private final IdSequence ids;
    private final ActorDirectory actors;
    public ReservationService(Inventory inventory, PickingContext picking, ValidationHandler chain,
                              Clock clock, IdSequence ids, ActorDirectory actors) {
        this.inventory=Objects.requireNonNull(inventory); this.picking=Objects.requireNonNull(picking);
        this.chain=Objects.requireNonNull(chain); this.clock=Objects.requireNonNull(clock);
        this.ids=Objects.requireNonNull(ids); this.actors=Objects.requireNonNull(actors);
    }
    public OperationResult reserve(String actor,String sku,String warehouse,int quantity,String manualOrAuto) {
        LocalDate today=LocalDate.now(clock);
        // Strategy runs BEFORE Chain, not inside the lot validation handler.
        List<Allocation> plan=picking.plan(inventory,sku,warehouse,manualOrAuto,quantity,today);
        OperationRequest request=OperationRequest.reserve(actor,sku,warehouse,quantity,plan);
        ValidationResult v=chain.handle(new ValidationContext(request,inventory,today));
        if (!v.valid()) return OperationResult.fail(v.rejectedBy(),v.reason());
        for (Allocation a:plan) inventory.lot(a.lotId()).reserve(a.quantity());
        inventory.warehouse(warehouse).stage(quantity);
        String id=ids.next("RES");
        inventory.addReservation(new Reservation(id,actor,sku,warehouse,plan,Instant.now(clock)));
        return OperationResult.ok(id,"Reserved "+quantity+" units in "+plan.size()+" lot(s): "+plan);
    }
    public OperationResult release(String actor,String reservationId) {
        OperationRequest request=OperationRequest.release(actor,reservationId);
        ValidationResult v=chain.handle(new ValidationContext(request,inventory,LocalDate.now(clock)));
        if (!v.valid()) return OperationResult.fail(v.rejectedBy(),v.reason());
        Reservation r=inventory.reservation(reservationId);
        for (Allocation a:r.allocations()) inventory.lot(a.lotId()).release(a.quantity());
        inventory.warehouse(r.warehouseId()).unstage(r.quantity());
        r.markReleased();
        return OperationResult.ok(r.id(),"Released reservation "+r.id());
    }
    public OperationResult restore(String actor,String reservationId) {
        Reservation r=inventory.reservation(reservationId);
        if (r==null || r.active()) return OperationResult.fail("RESOURCE","Reservation not found or already active");
        if (actors.roleOf(actor)==null || (!r.owner().equals(actor) && !actors.isSupervisor(actor)))
            return OperationResult.fail("AUTHORIZATION","Only owner/supervisor can restore a reservation");
        // Restore exact original allocations, again passing safety checks.
        OperationRequest request=OperationRequest.reserve(actor,r.sku(),r.warehouseId(),r.quantity(),r.allocations());
        ValidationResult v=chain.handle(new ValidationContext(request,inventory,LocalDate.now(clock)));
        if (!v.valid()) return OperationResult.fail(v.rejectedBy(),v.reason());
        for (Allocation a:r.allocations()) inventory.lot(a.lotId()).reserve(a.quantity());
        inventory.warehouse(r.warehouseId()).stage(r.quantity());
        r.restore();
        return OperationResult.ok(r.id(),"Restored reservation "+r.id());
    }
}
