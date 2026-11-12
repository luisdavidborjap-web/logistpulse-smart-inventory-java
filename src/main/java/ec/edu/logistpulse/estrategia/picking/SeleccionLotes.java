package ec.edu.logistpulse.estrategia.picking;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.dominio.Lote;
import ec.edu.logistpulse.validacion.ValidacionException;
import java.time.LocalDate;
import java.util.Comparator;

/** Filtro compartido de seguridad; las políticas solo cambian el orden. */
final class SeleccionLotes {
    private SeleccionLotes() { }

    static Lote seleccionar(Inventario inventario, String sku, int cantidad,
                            LocalDate hoy, Comparator<Lote> orden) {
        return inventario.lotes().stream()
                .filter(l -> l.getSku().equals(sku))
                .filter(l -> !l.isEnCuarentena() && !l.estaVencido(hoy))
                .filter(l -> l.getDisponible() >= cantidad)
                .min(orden)
                .orElseThrow(() -> new ValidacionException(
                        "No existe un lote habilitado con " + cantidad +
                        " unidades para " + sku + " (la reserva no se divide entre lotes)"));
    }
}
