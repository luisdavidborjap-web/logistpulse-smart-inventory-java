package ec.edu.logistpulse.bootstrap;

import ec.edu.logistpulse.dominio.*;
import java.time.LocalDate;

/** Inventario inicial para pruebas académicas. */
public final class DatosEjemplo {
    private DatosEjemplo() { }

    public static Inventario crear() {
        Inventario inv = new Inventario(new Almacen(8));
        inv.agregarProducto(new Producto("MED-001", "Vacunas"));
        inv.agregarProducto(new Producto("MED-002", "Guantes"));
        inv.agregarProducto(new Producto("TEC-001", "Sensores IoT"));
        inv.agregarLote(new Lote("L-100", "MED-001", 10, LocalDate.of(2099, 12, 31), false));
        inv.agregarLote(new Lote("L-200", "MED-002", 6, LocalDate.of(2099, 12, 31), false));
        inv.agregarLote(new Lote("L-201", "MED-002", 5, LocalDate.of(2099, 12, 31), true));
        inv.agregarLote(new Lote("L-202", "MED-002", 4, LocalDate.of(2020, 1, 1), false));
        inv.agregarLote(new Lote("L-300", "TEC-001", 3, LocalDate.of(2099, 12, 31), false));
        return inv;
    }
}
