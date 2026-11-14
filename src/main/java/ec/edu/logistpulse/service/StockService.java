package ec.edu.logistpulse.service;
import java.util.Objects;
/** Small facade receiver. Command depends on StockOperations, not this concrete class. */
public final class StockService implements StockOperations {
    private final ReservationService reservations;
    private final StockMutationService mutations;
    public StockService(ReservationService reservations,StockMutationService mutations) {
        this.reservations=Objects.requireNonNull(reservations);
        this.mutations=Objects.requireNonNull(mutations);
    }
    public synchronized OperationResult reserve(String actor,String sku,String warehouse,int qty,String lot) {
        return reservations.reserve(actor,sku,warehouse,qty,lot);
    }
    public synchronized OperationResult release(String actor,String reservationId) {
        return reservations.release(actor,reservationId);
    }
    public synchronized OperationResult restore(String actor,String reservationId) {
        return reservations.restore(actor,reservationId);
    }
    public synchronized OperationResult adjust(String actor,String lotId,int delta) {
        return mutations.adjust(actor,lotId,delta);
    }
    public synchronized OperationResult transfer(String actor,String lotId,String targetWarehouse,int qty) {
        return mutations.transfer(actor,lotId,targetWarehouse,qty);
    }
    public synchronized OperationResult undoTransfer(String actor,String lotId,String transferredLot,int qty) {
        return mutations.undoTransfer(actor,lotId,transferredLot,qty);
    }
}
