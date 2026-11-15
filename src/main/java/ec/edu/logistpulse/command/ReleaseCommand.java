package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.*;
public final class ReleaseCommand extends AbstractStockCommand {
    private final String reservationId;
    public ReleaseCommand(String id,String actor,String reservationId,StockOperations receiver) {
        super(id,actor,receiver);this.reservationId=reservationId;
    }
    public String type() {return "RELEASE";}
    public String targetId() {return reservationId;}
    public String fingerprint() {return Fingerprint.of(type(),actor(),reservationId);}
    public OperationResult execute() {return receiver.release(actor(),reservationId);}
    public OperationResult undo(String actor) {return receiver.restore(actor,reservationId);}
}
