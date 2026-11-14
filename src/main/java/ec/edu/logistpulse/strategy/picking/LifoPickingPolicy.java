package ec.edu.logistpulse.strategy.picking;
import ec.edu.logistpulse.domain.StockLot;
import java.util.Comparator;
public final class LifoPickingPolicy implements PickingPolicy {
    public String name() {return "LIFO";}
    public Comparator<StockLot> ordering() {return Comparator.comparing(StockLot::receivedAt).reversed().thenComparing(StockLot::id);}
}
