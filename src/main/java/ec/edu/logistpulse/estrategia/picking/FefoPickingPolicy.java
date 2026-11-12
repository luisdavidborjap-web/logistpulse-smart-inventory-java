package ec.edu.logistpulse.estrategia.picking;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.dominio.Lote;
import java.time.LocalDate;
import java.util.Comparator;

/** FEFO: First Expired, First Out (vence antes, sale antes). */
public final class FefoPickingPolicy implements PickingPolicy {
    private static final Comparator<Lote> ORDEN = Comparator
            .comparing(Lote::getVencimiento)
            .thenComparing(Lote::getFechaRecepcion)
            .thenComparing(Lote::getId);

    public String nombre() { return "FEFO"; }

    public Lote seleccionar(Inventario inventario, String sku, int cantidad, LocalDate hoy) {
        return SeleccionLotes.seleccionar(inventario, sku, cantidad, hoy, ORDEN);
    }
}
