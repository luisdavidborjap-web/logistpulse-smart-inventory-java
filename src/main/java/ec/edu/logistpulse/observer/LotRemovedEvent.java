package ec.edu.logistpulse.observer;
import java.time.Instant;
public record LotRemovedEvent(Instant occurredAt,String actor,String operation,String lotId,String sku)
        implements InventoryEvent { }
