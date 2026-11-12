package ec.edu.logistpulse.validacion;

import ec.edu.logistpulse.servicio.ContextoReserva;
import ec.edu.logistpulse.servicio.SolicitudReserva;

public class ValidarStock extends ManejadorReserva {
    @Override
    protected void validar(ContextoReserva c) {
        SolicitudReserva s = c.solicitud();
        int unidades = c.inventario().stockReservable(s.sku(), c.hoy());
        if (unidades < s.cantidad()) {
            throw new ValidacionException("Stock insuficiente para SKU " + s.sku()
                    + ": disponible=" + unidades + ", solicitado=" + s.cantidad());
        }
    }
}
