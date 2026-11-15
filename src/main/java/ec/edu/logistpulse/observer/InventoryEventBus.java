package ec.edu.logistpulse.observer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
/** Synchronous Observer implementation. Failed subscribers never roll back a committed mutation. */
public final class InventoryEventBus implements InventoryEventPublisher {
    private final CopyOnWriteArrayList<InventoryEventListener> listeners=new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<String> failures=new CopyOnWriteArrayList<>();
    public void subscribe(InventoryEventListener listener) {listeners.addIfAbsent(Objects.requireNonNull(listener));}
    public void unsubscribe(InventoryEventListener listener) {listeners.remove(listener);}
    public List<String> deliveryFailures() {return List.copyOf(failures);}
    public void publish(InventoryEvent event) {
        Objects.requireNonNull(event);
        for (InventoryEventListener listener:listeners) {
            try {listener.onEvent(event);}
            catch (RuntimeException ex) {
                failures.add(listener.getClass().getSimpleName()+": "+ex.getClass().getSimpleName());
            }
        }
    }
}
