package ec.edu.logistpulse.observer;
import java.time.Instant;
public record StockChangedEvent(Instant occurredAt,String actor,String operation,String lotId,
                                String sku,String warehouseId,int beforeOnHand,int afterOnHand,
                                int beforeReserved,int afterReserved) implements InventoryEvent { }
