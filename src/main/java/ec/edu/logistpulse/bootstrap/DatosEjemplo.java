package ec.edu.logistpulse.bootstrap;

import ec.edu.logistpulse.dominio.*;
import ec.edu.logistpulse.estrategia.reposicion.ParametrosReposicion;
import java.time.LocalDate;

/** Lotes ficticios con recepción y vencimiento que hacen visible FEFO/FIFO/LIFO. */
public final class DatosEjemplo {
    private DatosEjemplo() { }

    public static Inventario crear() {
        Inventario inv = new Inventario(new Almacen(8));
        inv.agregarProducto(new Producto("MED-001", "Vacunas"));
        inv.agregarProducto(new Producto("MED-002", "Guantes"));
        inv.agregarProducto(new Producto("TEC-001", "Sensores IoT"));
        inv.agregarLote(new Lote("L-100", "MED-001", 10, LocalDate.of(2099, 12, 31), false,
                LocalDate.of(2026, 8, 15)));
        // Tres lotes elegibles: FEFO -> L-200; FIFO -> L-203; LIFO -> L-204.
        inv.agregarLote(new Lote("L-200", "MED-002", 6, LocalDate.of(2027, 2, 15), false,
                LocalDate.of(2026, 9, 10)));
        inv.agregarLote(new Lote("L-203", "MED-002", 8, LocalDate.of(2028, 12, 31), false,
                LocalDate.of(2026, 8, 1)));
        inv.agregarLote(new Lote("L-204", "MED-002", 7, LocalDate.of(2029, 6, 30), false,
                LocalDate.of(2026, 11, 1)));
        inv.agregarLote(new Lote("L-201", "MED-002", 5, LocalDate.of(2099, 12, 31), true,
                LocalDate.of(2026, 7, 1)));
        inv.agregarLote(new Lote("L-202", "MED-002", 4, LocalDate.of(2020, 1, 1), false,
                LocalDate.of(2019, 8, 1)));
        inv.agregarLote(new Lote("L-300", "TEC-001", 3, LocalDate.of(2099, 12, 31), false,
                LocalDate.of(2026, 10, 1)));
        return inv;
    }

    /** Parámetros de ejemplo: no corresponden a datos reales de una empresa. */
    public static ParametrosReposicion parametrosReposicion(String sku, int stockActual) {
        return switch (sku) {
            case "MED-001" -> new ParametrosReposicion(sku, stockActual, 2, 3, 2, 25, 730, 3, 10);
            case "MED-002" -> new ParametrosReposicion(sku, stockActual, 4, 3, 4, 40, 1460, 3, 10);
            case "TEC-001" -> new ParametrosReposicion(sku, stockActual, 1, 5, 2, 15, 365, 2, 12);
            default -> throw new IllegalArgumentException("SKU sin parámetros de reposición: " + sku);
        };
    }
}
