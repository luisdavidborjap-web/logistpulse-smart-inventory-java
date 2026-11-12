package ec.edu.logistpulse.estrategia.picking;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.dominio.Lote;
import java.time.LocalDate;
import java.util.Comparator;

/** LIFO: Last In, First Out (recibido después, sale antes). */
public final class LifoPickingPolicy implements PickingPolicy {
    private static final Comparator<Lote> ORDEN = Comparator
            .comparing(Lote::getFechaRecepcion).reversed()
            .thenComparing(Lote::getVencimiento)
            .thenComparing(Lote::getId);

    public String nombre() { return "LIFO"; }

    public Lote seleccionar(Inventario inventario, String sku, int cantidad, LocalDate hoy) {
        return SeleccionLotes.seleccionar(inventario, sku, cantidad, hoy, ORDEN);
    }
}
