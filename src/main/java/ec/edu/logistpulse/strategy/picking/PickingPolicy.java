package ec.edu.logistpulse.strategy.picking;
import ec.edu.logistpulse.domain.StockLot;
import java.util.Comparator;
/** Strategy contract: only defines ordering; validation and execution live elsewhere. */
public interface PickingPolicy {
    String name();
    Comparator<StockLot> ordering();
}
