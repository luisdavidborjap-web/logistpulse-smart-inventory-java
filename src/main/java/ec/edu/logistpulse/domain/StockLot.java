package ec.edu.logistpulse.domain;
import java.time.LocalDate;
import java.util.Objects;
public final class StockLot {
    private final String id, sku, warehouseId;
    private final LocalDate receivedAt, expiresAt;
    private boolean quarantine;
    private int onHand, reserved;
    public StockLot(String id, String sku, String warehouseId, int onHand,
                    LocalDate receivedAt, LocalDate expiresAt, boolean quarantine) {
        if (id == null || id.isBlank() || sku == null || sku.isBlank()
                || warehouseId == null || warehouseId.isBlank() || onHand < 0)
            throw new IllegalArgumentException("Invalid lot");
        this.id=id; this.sku=sku; this.warehouseId=warehouseId; this.onHand=onHand;
        this.receivedAt=Objects.requireNonNull(receivedAt);
        this.expiresAt=Objects.requireNonNull(expiresAt); this.quarantine=quarantine;
    }
    public String id() { return id; }
    public String sku() { return sku; }
    public String warehouseId() { return warehouseId; }
    public LocalDate receivedAt() { return receivedAt; }
    public LocalDate expiresAt() { return expiresAt; }
    public boolean quarantine() { return quarantine; }
    public int onHand() { return onHand; }
    public int reserved() { return reserved; }
    public int available() { return onHand-reserved; }
    public boolean expired(LocalDate today) { return !expiresAt.isAfter(today); }
    public boolean eligible(LocalDate today) { return !quarantine && !expired(today); }
    public void reserve(int qty) {
        if (qty <= 0 || qty > available()) throw new IllegalStateException("Insufficient available lot stock");
        reserved += qty;
    }
    public void release(int qty) {
        if (qty <= 0 || qty > reserved) throw new IllegalStateException("Invalid lot release");
        reserved -= qty;
    }
    public void adjust(int delta) {
        long next=(long)onHand+delta;
        if (delta == 0 || next < reserved || next > Integer.MAX_VALUE)
            throw new IllegalStateException("Adjustment violates lot invariants");
        onHand=(int)next;
    }
}
