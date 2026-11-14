package ec.edu.logistpulse.strategy.replenishment;
public interface ReplenishmentPolicy {
    String name();
    ReplenishmentRecommendation recommend(ReplenishmentParameters parameters);
}
