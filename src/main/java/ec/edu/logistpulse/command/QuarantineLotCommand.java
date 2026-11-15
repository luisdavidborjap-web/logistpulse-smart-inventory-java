package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.*;
public final class QuarantineLotCommand extends AbstractStockCommand {
    private final String lotId;
    public QuarantineLotCommand(String id,String actor,String lotId,StockOperations receiver) {
        super(id,actor,receiver);this.lotId=lotId;
    }
    public String type() {return "QUARANTINE";}
    public String targetId() {return lotId;}
    public String fingerprint() {return Fingerprint.of(type(),actor(),lotId);}
    public OperationResult execute() {return receiver.quarantine(actor(),lotId);}
    public OperationResult undo(String actor) {return receiver.reinstate(actor,lotId);}
}
