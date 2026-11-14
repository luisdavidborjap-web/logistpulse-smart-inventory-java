package ec.edu.logistpulse.audit;
import java.util.List;
public interface AuditLedger {
    void record(AuditEntry entry);
    List<AuditEntry> entries();
}
