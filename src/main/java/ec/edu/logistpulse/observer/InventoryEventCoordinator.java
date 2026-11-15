package ec.edu.logistpulse.observer;
import ec.edu.logistpulse.domain.*;
import ec.edu.logistpulse.service.OperationResult;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
/** Application-level event emission, separate from domain and validation logic.
 *  Snapshot diffing ensures failures do not generate fictional success events.
 */
public final class InventoryEventCoordinator {
    private record LotSnapshot(String sku,String warehouse,int onHand,int reserved,String state) { }
    private record ReservationSnapshot(String owner,ReservationStatus status) { }
    private record Snapshot(Map<String,LotSnapshot> lots,Map<String,ReservationSnapshot> reservations) { }
    private final Inventory inventory;
    private final InventoryEventPublisher publisher;
    private final Clock clock;
    public InventoryEventCoordinator(Inventory inventory,InventoryEventPublisher publisher,Clock clock) {
        this.inventory=Objects.requireNonNull(inventory);
        this.publisher=Objects.requireNonNull(publisher);
        this.clock=Objects.requireNonNull(clock);
    }
    public OperationResult apply(String actor,String operation,Supplier<OperationResult> action) {
        Snapshot before=snapshot();
        OperationResult result=Objects.requireNonNull(action.get());
        if (result.successful()) publishChanges(before,snapshot(),actor,operation);
        return result;
    }
    private Snapshot snapshot() {
        Map<String,LotSnapshot> lots=new LinkedHashMap<>();
        for (StockLot l:inventory.lots())
            lots.put(l.id(),new LotSnapshot(l.sku(),l.warehouseId(),l.onHand(),l.reserved(),l.stateName()));
        Map<String,ReservationSnapshot> reservations=new LinkedHashMap<>();
        for (Reservation r:inventory.reservations())
            reservations.put(r.id(),new ReservationSnapshot(r.owner(),r.status()));
        return new Snapshot(lots,reservations);
    }
    private void publishChanges(Snapshot before,Snapshot after,String actor,String operation) {
        Instant timestamp=Instant.now(clock);
        for (var entry:after.lots.entrySet()) {
            String lotId=entry.getKey();LotSnapshot n=entry.getValue();LotSnapshot old=before.lots.get(lotId);
            if (old==null || old.onHand!=n.onHand || old.reserved!=n.reserved)
                publisher.publish(new StockChangedEvent(timestamp,actor,operation,lotId,n.sku,n.warehouse,
                        old==null?0:old.onHand,n.onHand,old==null?0:old.reserved,n.reserved));
            if (old==null || !old.state.equals(n.state))
                publisher.publish(new LotStateChangedEvent(timestamp,actor,operation,lotId,n.sku,
                        old==null?"NONE":old.state,n.state));
        }
        // A removed transfer lot is also a real stock change (zero units remaining).
        for (var entry:before.lots.entrySet()) {
            if (!after.lots.containsKey(entry.getKey())) {
                LotSnapshot old=entry.getValue();
                publisher.publish(new StockChangedEvent(timestamp,actor,operation,entry.getKey(),
                        old.sku,old.warehouse,old.onHand,0,old.reserved,0));
                publisher.publish(new LotRemovedEvent(timestamp,actor,operation,entry.getKey(),old.sku));
            }
        }
        for (var entry:after.reservations.entrySet()) {
            String id=entry.getKey();ReservationSnapshot n=entry.getValue();
            ReservationSnapshot old=before.reservations.get(id);
            if (old==null || old.status!=n.status)
                publisher.publish(new ReservationChangedEvent(timestamp,actor,operation,id,n.owner,
                        old==null?null:old.status,n.status));
        }
    }
}
