package ec.edu.logistpulse.strategy.replenishment;
public record ReplenishmentParameters(String sku, int stock, int dailyDemand, int leadTimeDays,
                                      int safetyStock, int maxStock, int yearlyDemand,
                                      double orderCost, double holdingCost) {
    public ReplenishmentParameters {
        if (sku==null || sku.isBlank() || stock<0 || dailyDemand<0 || leadTimeDays<0
                || safetyStock<0 || maxStock<0 || yearlyDemand<0
                || !Double.isFinite(orderCost) || !Double.isFinite(holdingCost)
                || orderCost<0 || holdingCost<=0)
            throw new IllegalArgumentException("Invalid replenishment parameters");
        long threshold=(long)dailyDemand*leadTimeDays+safetyStock;
        if (threshold>Integer.MAX_VALUE || maxStock<threshold)
            throw new IllegalArgumentException("Invalid reorder point / maximum stock");
    }
    public int reorderPoint() {return Math.addExact(Math.multiplyExact(dailyDemand,leadTimeDays),safetyStock);}
}
