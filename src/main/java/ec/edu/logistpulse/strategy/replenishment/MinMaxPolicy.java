package ec.edu.logistpulse.strategy.replenishment;
public final class MinMaxPolicy implements ReplenishmentPolicy {
    public String name() {return "MIN_MAX";}
    public ReplenishmentRecommendation recommend(ReplenishmentParameters p) {
        int q=p.stock()<=p.reorderPoint()? Math.max(0,p.maxStock()-p.stock()):0;
        return new ReplenishmentRecommendation(p.sku(),name(),p.reorderPoint(),q,"Replenish up to configured maximum");
    }
}
