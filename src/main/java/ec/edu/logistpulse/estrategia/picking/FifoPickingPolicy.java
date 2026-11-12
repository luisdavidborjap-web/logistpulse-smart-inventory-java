package ec.edu.logistpulse.estrategia.picking;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.dominio.Lote;
import java.time.LocalDate;
import java.util.Comparator;

/** FIFO: First In, First Out (recibido antes, sale antes). */
public final class FifoPickingPolicy implements PickingPolicy {
    private static final Comparator<Lote> ORDEN = Comparator
            .comparing(Lote::getFechaRecepcion)
            .thenComparing(Lote::getVencimiento)
            .thenComparing(Lote::getId);

    public String nombre() { return "FIFO"; }

    public Lote seleccionar(Inventario inventario, String sku, int cantidad, LocalDate hoy) {
        return SeleccionLotes.seleccionar(inventario, sku, cantidad, hoy, ORDEN);
    }
}
