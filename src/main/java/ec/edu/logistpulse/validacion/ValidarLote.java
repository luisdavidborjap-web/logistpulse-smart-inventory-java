package ec.edu.logistpulse.validacion;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.dominio.Lote;
import ec.edu.logistpulse.servicio.SolicitudReserva;
import java.time.LocalDate;

public class ValidarLote extends ManejadorReserva {
    @Override
    protected void validar(SolicitudReserva s, Inventario inventario, LocalDate hoy) {
        Lote lote = inventario.buscarLote(s.loteId());
        if (lote == null) throw new ValidacionException("Lote inexistente: " + s.loteId());
        if (!lote.getSku().equals(s.sku())) {
            throw new ValidacionException("El lote no pertenece al SKU solicitado");
        }
        if (lote.estaVencido(hoy)) throw new ValidacionException("Lote vencido: " + s.loteId());
        if (lote.isEnCuarentena()) throw new ValidacionException("Lote en cuarentena: " + s.loteId());
        if (lote.getDisponible() < s.cantidad()) {
            throw new ValidacionException("Unidades insuficientes en el lote: " + s.loteId());
        }
    }
}
