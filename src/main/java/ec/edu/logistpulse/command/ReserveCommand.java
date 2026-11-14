package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.*;
public final class ReserveCommand extends AbstractStockCommand {
    private final String sku,warehouseId,lotOrAuto;
    private final int quantity;
    private String createdReservationId;
    public ReserveCommand(String id,String actor,String sku,String warehouseId,int quantity,String lotOrAuto,StockOperations receiver) {
        super(id,actor,receiver);
        this.sku=sku;this.warehouseId=warehouseId;this.quantity=quantity;this.lotOrAuto=lotOrAuto;
    }
    public String type() {return "RESERVE";}
    public String targetId() {return sku;}
    public String fingerprint() {return type()+"|"+actor()+"|"+sku+"|"+warehouseId+"|"+quantity+"|"+lotOrAuto;}
    public OperationResult execute() {
        OperationResult result=receiver.reserve(actor(),sku,warehouseId,quantity,lotOrAuto);
        if (result.successful()) createdReservationId=result.objectId();
        return result;
    }
    public OperationResult undo(String actor) {
        if (createdReservationId==null) return OperationResult.fail("UNDO","No successful reservation to undo");
        return receiver.release(actor,createdReservationId);
    }
}
