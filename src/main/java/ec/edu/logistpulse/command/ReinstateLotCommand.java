package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.*;
public final class ReinstateLotCommand extends AbstractStockCommand {
    private final String lotId;
    public ReinstateLotCommand(String id,String actor,String lotId,StockOperations receiver) {
        super(id,actor,receiver);this.lotId=lotId;
    }
    public String type() {return "REINSTATE";}
    public String targetId() {return lotId;}
    public String fingerprint() {return Fingerprint.of(type(),actor(),lotId);}
    public OperationResult execute() {return receiver.reinstate(actor(),lotId);}
    public OperationResult undo(String actor) {return receiver.quarantine(actor,lotId);}
}
