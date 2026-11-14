package ec.edu.logistpulse.strategy.picking;
import ec.edu.logistpulse.domain.StockLot;
import java.util.Comparator;
public final class FefoPickingPolicy implements PickingPolicy {
    public String name() {return "FEFO";}
    public Comparator<StockLot> ordering() {return Comparator.comparing(StockLot::expiresAt).thenComparing(StockLot::receivedAt).thenComparing(StockLot::id);}
}
