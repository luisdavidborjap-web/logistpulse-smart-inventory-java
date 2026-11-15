package ec.edu.logistpulse.observer;
import ec.edu.logistpulse.domain.Inventory;
import ec.edu.logistpulse.strategy.replenishment.*;
import java.time.*;
import java.util.*;
import java.util.function.BiFunction;
/** Replenishment consumes events, but never modifies inventory automatically. */
public final class ReplenishmentAlertListener implements InventoryEventListener {
    private final Inventory inventory;
    private final ReplenishmentContext replenishment;
    private final Clock clock;
    private final BiFunction<String,Integer,ReplenishmentParameters> parameters;
    private final List<String> notifications=new ArrayList<>();
    public ReplenishmentAlertListener(Inventory inventory,ReplenishmentContext replenishment,Clock clock,
                   BiFunction<String,Integer,ReplenishmentParameters> parameters) {
        this.inventory=Objects.requireNonNull(inventory);this.replenishment=Objects.requireNonNull(replenishment);
        this.clock=Objects.requireNonNull(clock);this.parameters=Objects.requireNonNull(parameters);
    }
    public synchronized void onEvent(InventoryEvent event) {
        String sku,warehouse;
        if (event instanceof StockChangedEvent e) {
            sku=e.sku();warehouse=e.warehouseId();
        } else if (event instanceof LotStateChangedEvent e &&
                (e.operation().equals("QUARANTINE") || e.operation().equals("REINSTATE")
                 || e.operation().equals("STORE"))) {
            sku=e.sku();
            var lot=inventory.lot(e.lotId());
            if (lot==null) return;
            warehouse=lot.warehouseId();
        } else return;
        int available=inventory.available(sku,warehouse,LocalDate.now(clock));
        ReplenishmentRecommendation recommendation=replenishment.recommend(parameters.apply(sku,available));
        if (recommendation.suggestedQuantity()>0)
            notifications.add("REPLENISH " + sku + " at " + warehouse + ": " + recommendation);
    }
    public synchronized List<String> notifications() {return List.copyOf(notifications);}
}
