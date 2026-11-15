package ec.edu.logistpulse.observer;
import ec.edu.logistpulse.domain.ReservationStatus;
import java.util.*;
/** In-memory PULSEFLEET dispatch simulation (no external robot integration). */
public final class PulseFleetListener implements InventoryEventListener {
    private final List<String> notifications=new ArrayList<>();
    public synchronized void onEvent(InventoryEvent event) {
        if (!(event instanceof ReservationChangedEvent e)) return;
        if (e.currentStatus()==ReservationStatus.ACTIVE && e.previousStatus()==null)
            notifications.add("PULSEFLEET picking task for " + e.reservationId());
        if (e.currentStatus()==ReservationStatus.SHIPPED)
            notifications.add("PULSEFLEET shipment completed for " + e.reservationId());
    }
    public synchronized List<String> notifications() {return List.copyOf(notifications);}
}
