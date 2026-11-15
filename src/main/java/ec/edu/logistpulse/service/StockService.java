package ec.edu.logistpulse.service;
import ec.edu.logistpulse.observer.InventoryEventCoordinator;
import java.util.Objects;
/** Facade receiver; execution and event propagation are separate responsibilities. */
public final class StockService implements StockOperations {
    private final ReservationService reservations;
    private final StockMutationService mutations;
    private final InventoryEventCoordinator events;
    public StockService(ReservationService reservations,StockMutationService mutations,InventoryEventCoordinator events) {
        this.reservations=Objects.requireNonNull(reservations);
        this.mutations=Objects.requireNonNull(mutations);
        this.events=Objects.requireNonNull(events);
    }
    public synchronized OperationResult reserve(String actor,String sku,String wh,int qty,String lot) {
        return events.apply(actor,"RESERVE",()->reservations.reserve(actor,sku,wh,qty,lot));
    }
    public synchronized OperationResult release(String actor,String reservationId) {
        return events.apply(actor,"RELEASE",()->reservations.release(actor,reservationId));
    }
    public synchronized OperationResult restore(String actor,String reservationId) {
        return events.apply(actor,"RESTORE",()->reservations.restore(actor,reservationId));
    }
    public synchronized OperationResult ship(String actor,String reservationId) {
        return events.apply(actor,"SHIP",()->reservations.ship(actor,reservationId));
    }
    public synchronized OperationResult quarantine(String actor,String lotId) {
        return events.apply(actor,"QUARANTINE",()->mutations.quarantine(actor,lotId));
    }
    public synchronized OperationResult reinstate(String actor,String lotId) {
        return events.apply(actor,"REINSTATE",()->mutations.reinstate(actor,lotId));
    }
    public synchronized OperationResult store(String actor,String lotId) {
        return events.apply(actor,"STORE",()->mutations.store(actor,lotId));
    }
    public synchronized OperationResult unstore(String actor,String lotId) {
        return events.apply(actor,"UNSTORE",()->mutations.unstore(actor,lotId));
    }
    public synchronized OperationResult adjust(String actor,String lotId,int delta) {
        return events.apply(actor,"ADJUST",()->mutations.adjust(actor,lotId,delta));
    }
    public synchronized OperationResult transfer(String actor,String lotId,String targetWh,int qty) {
        return events.apply(actor,"TRANSFER",()->mutations.transfer(actor,lotId,targetWh,qty));
    }
    public synchronized OperationResult undoTransfer(String actor,String lotId,String movedLot,int qty) {
        return events.apply(actor,"UNDO_TRANSFER",()->mutations.undoTransfer(actor,lotId,movedLot,qty));
    }
}
