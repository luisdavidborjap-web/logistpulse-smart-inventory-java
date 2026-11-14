package ec.edu.logistpulse.domain;
public record Product(String sku, String name) {
    public Product { if (sku == null || sku.isBlank() || name == null || name.isBlank())
        throw new IllegalArgumentException("SKU and name are required"); }
}
