package ec.edu.logistpulse.state;
/** A received lot must be put away or quarantined before it may be reserved. */
public final class ReceivedState implements LotState {
    public String name() { return "RECEIVED"; }
    public LotState store() { return new StoredState(); }
    public LotState quarantine() { return new QuarantinedState(); }
    public void assertAdjustable() { }
}
