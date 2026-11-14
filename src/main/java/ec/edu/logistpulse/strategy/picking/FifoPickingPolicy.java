package ec.edu.logistpulse.strategy.picking;
import ec.edu.logistpulse.domain.StockLot;
import java.util.Comparator;
public final class FifoPickingPolicy implements PickingPolicy {
    public String name() {return "FIFO";}
    public Comparator<StockLot> ordering() {return Comparator.comparing(StockLot::receivedAt).thenComparing(StockLot::id);}
}
