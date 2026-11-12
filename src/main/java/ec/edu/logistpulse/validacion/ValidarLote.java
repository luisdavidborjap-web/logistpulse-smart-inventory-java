package ec.edu.logistpulse.validacion;

import ec.edu.logistpulse.dominio.Lote;
import ec.edu.logistpulse.servicio.ContextoReserva;
import ec.edu.logistpulse.servicio.SolicitudReserva;

/** El lote AUTO se resuelve con Strategy dentro del tercer eslabón de Chain. */
public class ValidarLote extends ManejadorReserva {
    @Override
    protected void validar(ContextoReserva c) {
        SolicitudReserva s = c.solicitud();
        if (s.loteId() == null || s.loteId().isBlank()) {
            throw new ValidacionException("Indique el ID de lote o AUTO");
        }

        Lote lote = s.loteId().equalsIgnoreCase("AUTO")
                ? c.pickingPolicy().seleccionar(c.inventario(), s.sku(), s.cantidad(), c.hoy())
                : c.inventario().buscarLote(s.loteId());

        if (lote == null) throw new ValidacionException("Lote inexistente: " + s.loteId());
        if (!lote.getSku().equals(s.sku())) {
            throw new ValidacionException("El lote no pertenece al SKU solicitado");
        }
        if (lote.estaVencido(c.hoy())) throw new ValidacionException("Lote vencido: " + lote.getId());
        if (lote.isEnCuarentena()) throw new ValidacionException("Lote en cuarentena: " + lote.getId());
        if (lote.getDisponible() < s.cantidad()) {
            throw new ValidacionException("Unidades insuficientes en el lote: " + lote.getId());
        }
        c.seleccionarLote(lote);
    }
}
