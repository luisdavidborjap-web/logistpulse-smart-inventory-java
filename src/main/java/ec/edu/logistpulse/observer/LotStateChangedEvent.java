package ec.edu.logistpulse.observer;
import java.time.Instant;
public record LotStateChangedEvent(Instant occurredAt,String actor,String operation,String lotId,
                                   String sku,String previousState,String currentState) implements InventoryEvent { }
