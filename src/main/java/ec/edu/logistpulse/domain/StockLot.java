package ec.edu.logistpulse.domain;
import ec.edu.logistpulse.state.*;
import java.time.LocalDate;
import java.util.Objects;
/** Aggregate root for lot invariants; operations delegate transitions to State objects. */
public final class StockLot {
    private final String id, sku, warehouseId;
    private final LocalDate receivedAt, expiresAt;
    private LotState state;
    private int onHand, reserved;
    private boolean canUndoPutAway;
    public StockLot(String id, String sku, String warehouseId, int onHand,
                    LocalDate receivedAt, LocalDate expiresAt, boolean quarantine) {
        this(id,sku,warehouseId,onHand,receivedAt,expiresAt,
                quarantine ? new QuarantinedState() : new StoredState());
    }
    private StockLot(String id,String sku,String warehouseId,int onHand,
                     LocalDate receivedAt,LocalDate expiresAt,LotState state) {
        if (id==null || id.isBlank() || sku==null || sku.isBlank()
                || warehouseId==null || warehouseId.isBlank() || onHand<0)
            throw new IllegalArgumentException("Invalid lot");
        this.id=id;this.sku=sku;this.warehouseId=warehouseId;this.onHand=onHand;
        this.receivedAt=Objects.requireNonNull(receivedAt);
        this.expiresAt=Objects.requireNonNull(expiresAt);
        if (expiresAt.isBefore(receivedAt)) throw new IllegalArgumentException("Expiry precedes receipt");
        this.state=Objects.requireNonNull(state);
    }
    /** Newly received goods are not eligible until they pass put-away. */
    public static StockLot received(String id,String sku,String warehouseId,int qty,
                                    LocalDate receivedAt,LocalDate expiresAt) {
        return new StockLot(id,sku,warehouseId,qty,receivedAt,expiresAt,new ReceivedState());
    }
    public String id() {return id;}
    public String sku() {return sku;}
    public String warehouseId() {return warehouseId;}
    public LocalDate receivedAt() {return receivedAt;}
    public LocalDate expiresAt() {return expiresAt;}
    public String stateName() {return state.name();}
    public boolean quarantine() {return state instanceof QuarantinedState;}
    public int onHand() {return onHand;}
    public int reserved() {return reserved;}
    public int available() {return onHand-reserved;}
    public boolean expired(LocalDate today) {return !expiresAt.isAfter(today);}
    public boolean eligible(LocalDate today) {
        return (state instanceof StoredState || state instanceof ReservedState) && !expired(today);
    }
    public void store() {state=state.store();canUndoPutAway=true;}
    public boolean canUndoStorage() {return canUndoPutAway && state instanceof StoredState && reserved==0;}
    /** Compensates a put-away only if no subsequent stock mutation has happened. */
    public void undoStore() {
        if (!canUndoStorage()) throw new IllegalStateException("Put-away already used or not reversible");
        state=new ReceivedState();canUndoPutAway=false;
    }
    public void quarantineLot() {
        if (reserved!=0) throw new IllegalStateException("Release reservations before quarantine");
        state=state.quarantine();canUndoPutAway=false;
    }
    public void reinstate() {state=state.reinstate();canUndoPutAway=false;}
    public void reserve(int qty) {
        if (qty<=0 || qty>available()) throw new IllegalStateException("Insufficient available lot stock");
        LotState next=state.reserve();
        reserved+=qty;state=next;canUndoPutAway=false;
    }
    public void release(int qty) {
        if (qty<=0 || qty>reserved) throw new IllegalStateException("Invalid lot release");
        LotState next=state.release(reserved-qty);
        reserved-=qty;state=next;canUndoPutAway=false;
    }
    /** Shipping consumes physically stored stock as well as the reservation. */
    public void ship(int qty) {
        if (qty<=0 || qty>reserved) throw new IllegalStateException("Invalid shipment quantity");
        LotState next=state.ship(onHand-qty,reserved-qty);
        onHand-=qty;reserved-=qty;state=next;canUndoPutAway=false;
    }
    public void adjust(int delta) {
        state.assertAdjustable();
        long next=(long)onHand+delta;
        if (delta==0 || next<reserved || next>Integer.MAX_VALUE)
            throw new IllegalStateException("Adjustment violates lot invariants");
        onHand=(int)next;canUndoPutAway=false;
    }
}
