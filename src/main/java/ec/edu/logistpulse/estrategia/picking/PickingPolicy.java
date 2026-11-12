package ec.edu.logistpulse.estrategia.picking;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.dominio.Lote;
import java.time.LocalDate;

/** Strategy: algoritmo intercambiable de selección de un lote para una reserva. */
public interface PickingPolicy {
    String nombre();
    Lote seleccionar(Inventario inventario, String sku, int cantidad, LocalDate hoy);
}
