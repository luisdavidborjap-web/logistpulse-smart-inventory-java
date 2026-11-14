package ec.edu.logistpulse.audit;
import java.time.Instant;
/** Each new command decision and each undo has explicit identity and target. */
public record AuditEntry(Instant at, String commandId, String operation,String actor,
                         String targetId, boolean successful, String rejectedBy,String detail) { }
