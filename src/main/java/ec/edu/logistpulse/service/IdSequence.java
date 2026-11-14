package ec.edu.logistpulse.service;
import java.util.HashMap;
import java.util.Map;
/** Human-readable sequential IDs in this running demo (not durable across restarts). */
public final class IdSequence {
    private final Map<String,Integer> counts=new HashMap<>();
    public synchronized String next(String prefix) {
        int n=counts.merge(prefix,1,Integer::sum);
        return "%s-%06d".formatted(prefix,n);
    }
}
