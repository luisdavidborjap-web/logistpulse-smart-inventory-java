package ec.edu.logistpulse.state;
public final class ReservedState implements LotState {
    public String name() { return "RESERVED"; }
    public LotState reserve() { return this; }
    public LotState release(int remainingReserved) {
        return remainingReserved == 0 ? new StoredState() : this;
    }
    public LotState ship(int remainingOnHand, int remainingReserved) {
        if (remainingReserved > 0) return this;
        return remainingOnHand == 0 ? new ShippedState() : new StoredState();
    }
    public void assertAdjustable() { }
}
