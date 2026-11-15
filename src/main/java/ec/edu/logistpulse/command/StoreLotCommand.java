package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.*;
/** Put-away can be compensated only before any further stock operation. */
public final class StoreLotCommand extends AbstractStockCommand {
    private final String lotId;
    public StoreLotCommand(String id,String actor,String lotId,StockOperations receiver) {
        super(id,actor,receiver);this.lotId=lotId;
    }
    public String type() {return "STORE";}
    public String targetId() {return lotId;}
    public String fingerprint() {return Fingerprint.of(type(),actor(),lotId);}
    public OperationResult execute() {return receiver.store(actor(),lotId);}
    public OperationResult undo(String actor) {return receiver.unstore(actor,lotId);}
}
