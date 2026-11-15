package ec.edu.logistpulse.observer;
import java.util.*;
/** Observer consumer / event ledger: stores immutable domain notifications. */
public final class InventoryEventJournal implements InventoryEventListener {
    private final List<InventoryEvent> events=new ArrayList<>();
    public synchronized void onEvent(InventoryEvent event) {events.add(event);}
    public synchronized List<InventoryEvent> entries() {return List.copyOf(events);}
}
