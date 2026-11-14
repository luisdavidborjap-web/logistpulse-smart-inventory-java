package ec.edu.logistpulse.strategy.replenishment;
public final class EoqPolicy implements ReplenishmentPolicy {
    public String name() {return "EOQ";}
    public ReplenishmentRecommendation recommend(ReplenishmentParameters p) {
        double raw=Math.sqrt((2.0*p.yearlyDemand()*p.orderCost())/p.holdingCost());
        if (!Double.isFinite(raw) || raw>Integer.MAX_VALUE)
            throw new IllegalArgumentException("EOQ quantity outside supported integer range");
        int q=p.stock()<=p.reorderPoint()?(int)Math.ceil(raw):0;
        return new ReplenishmentRecommendation(p.sku(),name(),p.reorderPoint(),q,
                "Economic order quantity (demo parameters)");
    }
}
