package ec.edu.logistpulse.command;
import ec.edu.logistpulse.service.OperationResult;
/** Command contract: both execute and compensating undo are explicit. */
public interface Command {
    String id();
    String type();
    String actor();
    String targetId();
    String fingerprint();
    OperationResult execute();
    OperationResult undo(String requestingActor);
    default OperationResult undo() {return undo(actor());}
}
