package ec.edu.logistpulse.state;
/** Terminal state of an exhausted lot after fulfilling all reserved units. */
public final class ShippedState implements LotState {
    public String name() { return "SHIPPED"; }
}
