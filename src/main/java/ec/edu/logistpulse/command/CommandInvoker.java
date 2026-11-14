package ec.edu.logistpulse.command;
import ec.edu.logistpulse.audit.*;
import ec.edu.logistpulse.security.ActorDirectory;
import ec.edu.logistpulse.service.OperationResult;
import java.time.*;
import java.util.*;
/** Idempotent invoker. Checks both command ID AND exact request fingerprint. */
public final class CommandInvoker {
    private static final class Executed {
        final Command command;
        final String fingerprint;
        final CommandResult result;
        CommandResult undoResult;
        Executed(Command c,String f,CommandResult r){command=c;fingerprint=f;result=r;}
    }
    private final Map<String,Executed> history=new LinkedHashMap<>();
    private final AuditLedger ledger;
    private final ActorDirectory actors;
    private final Clock clock;
    public CommandInvoker(AuditLedger ledger,ActorDirectory actors,Clock clock) {
        this.ledger=Objects.requireNonNull(ledger);this.actors=Objects.requireNonNull(actors);
        this.clock=Objects.requireNonNull(clock);
    }
    public synchronized CommandResult execute(Command c) {
        Objects.requireNonNull(c);
        Executed previous=history.get(c.id());
        if (previous!=null) {
            if (previous.fingerprint.equals(c.fingerprint())) {
                if (previous.undoResult!=null) return new CommandResult(c.id(),false,"","UNDO",
                        "Command was already compensated; create a new command ID");
                return previous.result;
            }
            CommandResult conflict=new CommandResult(c.id(),false,"","IDEMPOTENCY",
                    "Command ID reused with a different payload");
            record(c,"CONFLICT",conflict);
            return conflict;
        }
        OperationResult result;
        try {result=c.execute();}
        catch (RuntimeException e) {result=OperationResult.fail("EXECUTION",e.getMessage());}
        CommandResult out=new CommandResult(c.id(),result.successful(),result.objectId(),
                                            result.rejectedBy(),result.message());
        history.put(c.id(),new Executed(c,c.fingerprint(),out));
        record(c,c.type(),out);
        return out;
    }
    public synchronized CommandResult undo(String commandId,String requester) {
        Executed e=history.get(commandId);
        if (e==null) return new CommandResult(commandId,false,"","UNDO","Unknown command");
        if (!e.result.successful()) return new CommandResult(commandId,false,"","UNDO",
                                                        "Cannot undo a rejected command");
        if (actors.roleOf(requester)==null
                || (!e.command.actor().equals(requester) && !actors.isSupervisor(requester))) {
            CommandResult result=new CommandResult(commandId,false,"","AUTHORIZATION",
                    "Only command owner/supervisor may undo");
            record(e.command,"UNDO",requester,result);
            return result;
        }
        if (e.undoResult!=null) return e.undoResult;
        OperationResult outcome;
        try {outcome=e.command.undo(requester);}
        catch (RuntimeException ex) {outcome=OperationResult.fail("UNDO",ex.getMessage());}
        CommandResult result=new CommandResult(commandId,outcome.successful(),outcome.objectId(),
                                               outcome.rejectedBy(),outcome.message());
        if (result.successful()) e.undoResult=result;
        record(e.command,"UNDO",requester,result);
        return result;
    }
    private void record(Command c,String type,CommandResult r) {record(c,type,c.actor(),r);}
    private void record(Command c,String type,String actor,CommandResult r) {
        ledger.record(new AuditEntry(Instant.now(clock),c.id(),type,actor,c.targetId(),
                                     r.successful(),r.rejectedBy(),r.message()));
    }
}
