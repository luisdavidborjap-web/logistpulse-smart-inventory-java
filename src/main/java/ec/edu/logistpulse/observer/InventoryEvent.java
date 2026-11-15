package ec.edu.logistpulse.observer;
import java.time.Instant;
/** Immutable fact about a completed, successful inventory mutation. */
public sealed interface InventoryEvent permits StockChangedEvent, LotStateChangedEvent, ReservationChangedEvent, LotRemovedEvent {
    Instant occurredAt();
    String actor();
    String operation();
}
