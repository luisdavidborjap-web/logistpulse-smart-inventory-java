package ec.edu.logistpulse.validacion;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.servicio.SolicitudReserva;
import java.time.LocalDate;

public class ValidarStock extends ManejadorReserva {
    @Override
    protected void validar(SolicitudReserva s, Inventario inventario, LocalDate hoy) {
        int unidades = inventario.stockReservable(s.sku(), hoy);
        if (unidades < s.cantidad()) {
            throw new ValidacionException("Stock insuficiente para SKU " + s.sku()
                    + ": disponible=" + unidades + ", solicitado=" + s.cantidad());
        }
    }
}
