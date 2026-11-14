package ec.edu.logistpulse.audit;
import java.util.*;
public final class InMemoryAuditLedger implements AuditLedger {
    private final List<AuditEntry> entries=new ArrayList<>();
    public synchronized void record(AuditEntry entry) {entries.add(Objects.requireNonNull(entry));}
    public synchronized List<AuditEntry> entries() {return List.copyOf(entries);}
}
