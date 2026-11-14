package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.*;
public final class AdjustCommand extends AbstractStockCommand {
    private final String lotId;
    private final int delta;
    public AdjustCommand(String id,String actor,String lotId,int delta,StockOperations receiver) {
        super(id,actor,receiver);this.lotId=lotId;this.delta=delta;
    }
    public String type() {return "ADJUST";}
    public String targetId() {return lotId;}
    public String fingerprint() {return type()+"|"+actor()+"|"+lotId+"|"+delta;}
    public OperationResult execute() {return receiver.adjust(actor(),lotId,delta);}
    public OperationResult undo(String actor) {return receiver.adjust(actor,lotId,-delta);}
}
