package ec.edu.logistpulse.domain;
import java.time.LocalDate;
import java.util.*;
public final class Inventory {
    private final Map<String, Product> products = new LinkedHashMap<>();
    private final Map<String, Warehouse> warehouses = new LinkedHashMap<>();
    private final Map<String, StockLot> lots = new LinkedHashMap<>();
    private final Map<String, Reservation> reservations = new LinkedHashMap<>();
    public void addProduct(Product product) {
        Objects.requireNonNull(product);
        if (products.putIfAbsent(product.sku(),product)!=null) throw new IllegalArgumentException("Duplicate SKU");
    }
    public void addWarehouse(Warehouse warehouse) {
        Objects.requireNonNull(warehouse);
        if (warehouses.putIfAbsent(warehouse.id(),warehouse)!=null) throw new IllegalArgumentException("Duplicate warehouse");
    }
    public void addLot(StockLot lot) {
        Objects.requireNonNull(lot);
        if (product(lot.sku())==null || warehouse(lot.warehouseId())==null)
            throw new IllegalArgumentException("Unknown lot product/warehouse");
        if ((long)warehouseOnHand(lot.warehouseId())+lot.onHand()>warehouse(lot.warehouseId()).storageCapacity())
            throw new IllegalArgumentException("Warehouse storage capacity exceeded");
        if (lots.putIfAbsent(lot.id(),lot)!=null) throw new IllegalArgumentException("Duplicate lot ID");
    }
    public void removeLot(String lotId) {
        StockLot lot=lots.get(lotId);
        boolean referenced=reservations.values().stream()
            .anyMatch(r->r.allocations().stream().anyMatch(a->a.lotId().equals(lotId)));
        if (lot==null || lot.reserved()!=0 || referenced)
            throw new IllegalStateException("Cannot remove lot referenced by a reservation");
        lots.remove(lotId);
    }
    public void addReservation(Reservation reservation) {
        Objects.requireNonNull(reservation);
        if (reservations.putIfAbsent(reservation.id(),reservation)!=null) throw new IllegalArgumentException("Duplicate reservation");
    }
    public Product product(String sku) {return products.get(sku);}
    public Warehouse warehouse(String id) {return warehouses.get(id);}
    public StockLot lot(String id) {return lots.get(id);}
    public Reservation reservation(String id) {return reservations.get(id);}
    public Collection<Product> products() {return Collections.unmodifiableCollection(products.values());}
    public Collection<Warehouse> warehouses() {return Collections.unmodifiableCollection(warehouses.values());}
    public Collection<StockLot> lots() {return Collections.unmodifiableCollection(lots.values());}
    public Collection<Reservation> reservations() {return Collections.unmodifiableCollection(reservations.values());}
    public int warehouseOnHand(String warehouseId) {
        return lots.values().stream().filter(l->l.warehouseId().equals(warehouseId)).mapToInt(StockLot::onHand).sum();
    }
    public int available(String sku, String warehouseId, LocalDate today) {
        return lots.values().stream().filter(l->l.sku().equals(sku) && l.warehouseId().equals(warehouseId)
                && l.eligible(today)).mapToInt(StockLot::available).sum();
    }
}
