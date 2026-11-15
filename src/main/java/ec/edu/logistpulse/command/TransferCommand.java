package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.*;
public final class TransferCommand extends AbstractStockCommand {
    private final String sourceLotId,destinationWarehouseId;
    private final int quantity;
    private String transferredLotId;
    public TransferCommand(String id,String actor,String sourceLotId,String destinationWarehouseId,int quantity,
                           StockOperations receiver) {
        super(id,actor,receiver);this.sourceLotId=sourceLotId;
        this.destinationWarehouseId=destinationWarehouseId;this.quantity=quantity;
    }
    public String type() {return "TRANSFER";}
    public String targetId() {return sourceLotId;}
    public String fingerprint() {return Fingerprint.of(type(),actor(),sourceLotId,destinationWarehouseId,Integer.toString(quantity));}
    public OperationResult execute() {
        OperationResult result=receiver.transfer(actor(),sourceLotId,destinationWarehouseId,quantity);
        if (result.successful()) transferredLotId=result.objectId();
        return result;
    }
    public OperationResult undo(String actor) {
        if (transferredLotId==null) return OperationResult.fail("UNDO","No successful transfer to undo");
        return receiver.undoTransfer(actor,sourceLotId,transferredLotId,quantity);
    }
}
