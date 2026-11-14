package ec.edu.logistpulse.strategy.replenishment;
import java.util.*;
public final class ReplenishmentContext {
    private ReplenishmentPolicy defaultPolicy;
    private final Map<String,ReplenishmentPolicy> overrides=new HashMap<>();
    public ReplenishmentContext(ReplenishmentPolicy policy) {defaultPolicy=Objects.requireNonNull(policy);}
    public void changeDefault(ReplenishmentPolicy policy) {defaultPolicy=Objects.requireNonNull(policy);}
    public ReplenishmentPolicy defaultPolicy() {return defaultPolicy;}
    public void changeForSku(String sku, ReplenishmentPolicy policy) {
        if (sku==null || sku.isBlank()) throw new IllegalArgumentException("SKU required");
        overrides.put(sku,Objects.requireNonNull(policy));
    }
    public ReplenishmentRecommendation recommend(ReplenishmentParameters p) {
        return overrides.getOrDefault(p.sku(),defaultPolicy).recommend(p);
    }
}
