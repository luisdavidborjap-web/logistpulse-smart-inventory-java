package ec.edu.logistpulse.state;
/** State: transitions are determined by the current object, not a central switch. */
public sealed interface LotState permits ReceivedState, StoredState, ReservedState, ShippedState, QuarantinedState {
    String name();
    default LotState store() { throw invalid("store"); }
    default LotState reserve() { throw invalid("reserve"); }
    default LotState release(int remainingReserved) { throw invalid("release"); }
    default LotState ship(int remainingOnHand, int remainingReserved) { throw invalid("ship"); }
    default LotState quarantine() { throw invalid("quarantine"); }
    default LotState reinstate() { throw invalid("reinstate"); }
    default void assertAdjustable() { throw invalid("adjust"); }
    private IllegalStateException invalid(String action) {
        return new IllegalStateException("Cannot " + action + " a lot in state " + name());
    }
}
