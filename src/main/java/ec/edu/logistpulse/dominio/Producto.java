package ec.edu.logistpulse.dominio;

public record Producto(String sku, String nombre) {
    public Producto {
        if (sku == null || sku.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("SKU y nombre son obligatorios");
        }
    }
}
