package ec.edu.logistpulse.state;
public final class StoredState implements LotState {
    public String name() { return "STORED"; }
    public LotState reserve() { return new ReservedState(); }
    public LotState quarantine() { return new QuarantinedState(); }
    public void assertAdjustable() { }
}
