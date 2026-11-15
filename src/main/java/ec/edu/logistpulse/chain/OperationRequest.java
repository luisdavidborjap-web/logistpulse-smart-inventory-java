package ec.edu.logistpulse.chain;
import ec.edu.logistpulse.domain.Allocation;
import java.util.List;
/** One immutable request type shared by all four commands and validators. */
public record OperationRequest(OperationType type, String actor, String sku, String lotId,
        String warehouseId, String destinationWarehouseId, String reservationId,
        int quantity, List<Allocation> plan) {
    public OperationRequest { plan=plan==null?List.of():List.copyOf(plan); }
    public static OperationRequest reserve(String actor, String sku, String warehouseId, int quantity,
                                            List<Allocation> plan) {
        return new OperationRequest(OperationType.RESERVE,actor,sku,null,warehouseId,null,null,quantity,plan);
    }
    public static OperationRequest release(String actor, String reservationId) {
        return new OperationRequest(OperationType.RELEASE,actor,null,null,null,null,reservationId,0,List.of());
    }
    public static OperationRequest ship(String actor,String reservationId) {
        return new OperationRequest(OperationType.SHIP,actor,null,null,null,null,reservationId,0,List.of());
    }
    public static OperationRequest quarantine(String actor,String lotId) {
        return new OperationRequest(OperationType.QUARANTINE,actor,null,lotId,null,null,null,0,List.of());
    }
    public static OperationRequest reinstate(String actor,String lotId) {
        return new OperationRequest(OperationType.REINSTATE,actor,null,lotId,null,null,null,0,List.of());
    }
    public static OperationRequest store(String actor,String lotId) {
        return new OperationRequest(OperationType.STORE,actor,null,lotId,null,null,null,0,List.of());
    }
    public static OperationRequest unstore(String actor,String lotId) {
        return new OperationRequest(OperationType.UNSTORE,actor,null,lotId,null,null,null,0,List.of());
    }
    public static OperationRequest adjust(String actor, String lotId, int delta) {
        return new OperationRequest(OperationType.ADJUST,actor,null,lotId,null,null,null,delta,List.of());
    }
    public static OperationRequest transfer(String actor, String lotId, String destinationWarehouseId,int qty) {
        return new OperationRequest(OperationType.TRANSFER,actor,null,lotId,null,destinationWarehouseId,null,qty,List.of());
    }
}
