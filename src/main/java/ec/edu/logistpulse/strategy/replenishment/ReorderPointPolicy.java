package ec.edu.logistpulse.strategy.replenishment;
public final class ReorderPointPolicy implements ReplenishmentPolicy {
    public String name() {return "REORDER_POINT";}
    public ReplenishmentRecommendation recommend(ReplenishmentParameters p) {
        int threshold=p.reorderPoint();
        int order=p.stock()<=threshold ? Math.max(1,p.dailyDemand()*Math.max(1,p.leadTimeDays())) : 0;
        return new ReplenishmentRecommendation(p.sku(),name(),threshold,order,"Demand during replenishment lead time");
    }
}
