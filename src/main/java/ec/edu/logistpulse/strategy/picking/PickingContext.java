package ec.edu.logistpulse.strategy.picking;
import ec.edu.logistpulse.domain.*;
import java.time.LocalDate;
import java.util.*;
/** Dedicated Strategy context, independent of Chain and StockService. */
public final class PickingContext {
    private PickingPolicy defaultPolicy;
    private final Map<String, PickingPolicy> overrides = new HashMap<>();
    public PickingContext(PickingPolicy policy) {this.defaultPolicy=Objects.requireNonNull(policy);}
    public void changeDefault(PickingPolicy policy) {defaultPolicy=Objects.requireNonNull(policy);}
    public void changeForSku(String sku, PickingPolicy policy) {
        if (sku==null || sku.isBlank()) throw new IllegalArgumentException("SKU required");
        overrides.put(sku,Objects.requireNonNull(policy));
    }
    public PickingPolicy policyFor(String sku) {return overrides.getOrDefault(sku,defaultPolicy);}
    public PickingPolicy defaultPolicy() {return defaultPolicy;}
    /** Partial plan is returned when stock runs out; Chain rejects it with a reason. */
    public List<Allocation> plan(Inventory inventory, String sku, String warehouseId,
                                 String explicitLotId, int quantity, LocalDate date) {
        if (quantity<=0) return List.of();
        if (explicitLotId!=null && !explicitLotId.isBlank() && !explicitLotId.equalsIgnoreCase("AUTO"))
            return List.of(new Allocation(explicitLotId,quantity));
        List<StockLot> eligible=inventory.lots().stream()
            .filter(l->l.sku().equals(sku) && l.warehouseId().equals(warehouseId)
                        && l.eligible(date) && l.available()>0)
            .sorted(policyFor(sku).ordering()).toList();
        List<Allocation> planned=new ArrayList<>();
        int remaining=quantity;
        for (StockLot lot:eligible) {
            int take=Math.min(remaining,lot.available());
            if (take>0) planned.add(new Allocation(lot.id(),take));
            remaining-=take;
            if (remaining==0) break;
        }
        return List.copyOf(planned);
    }
}
