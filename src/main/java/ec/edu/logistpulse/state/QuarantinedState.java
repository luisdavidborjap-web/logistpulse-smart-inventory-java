package ec.edu.logistpulse.state;
/** No reservations while quality controls keep the batch in quarantine. */
public final class QuarantinedState implements LotState {
    public String name() { return "QUARANTINED"; }
    public LotState reinstate() { return new StoredState(); }
    public void assertAdjustable() { }
}
