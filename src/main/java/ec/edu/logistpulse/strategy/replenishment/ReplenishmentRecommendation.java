package ec.edu.logistpulse.strategy.replenishment;
public record ReplenishmentRecommendation(String sku, String policy, int reorderPoint, int suggestedQuantity,
                                         String explanation) { }
