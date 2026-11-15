package ec.edu.logistpulse.observer;
import java.time.Instant;
import ec.edu.logistpulse.domain.ReservationStatus;
public record ReservationChangedEvent(Instant occurredAt,String actor,String operation,String reservationId,
                                      String owner,ReservationStatus previousStatus,ReservationStatus currentStatus)
        implements InventoryEvent { }
