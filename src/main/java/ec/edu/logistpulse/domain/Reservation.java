package ec.edu.logistpulse.domain;
import java.time.Instant;
import java.util.List;
public final class Reservation {
    private final String id, owner, sku, warehouseId;
    private final List<Allocation> allocations;
    private final Instant createdAt;
    private boolean active = true;
    public Reservation(String id, String owner, String sku, String warehouseId,
                       List<Allocation> allocations, Instant createdAt) {
        if (id == null || id.isBlank() || owner == null || owner.isBlank()
                || sku == null || sku.isBlank() || warehouseId == null || warehouseId.isBlank()
                || allocations == null || allocations.isEmpty())
            throw new IllegalArgumentException("Invalid reservation");
        this.id=id; this.owner=owner; this.sku=sku; this.warehouseId=warehouseId;
        this.allocations=List.copyOf(allocations); this.createdAt=java.util.Objects.requireNonNull(createdAt);
    }
    public String id() {return id;}
    public String owner() {return owner;}
    public String sku() {return sku;}
    public String warehouseId() {return warehouseId;}
    public List<Allocation> allocations() {return allocations;}
    public Instant createdAt() {return createdAt;}
    public boolean active() {return active;}
    public int quantity() {return allocations.stream().mapToInt(Allocation::quantity).sum();}
    public void markReleased() {if (!active) throw new IllegalStateException("Already released"); active=false;}
    public void restore() {if (active) throw new IllegalStateException("Already active"); active=true;}
}
