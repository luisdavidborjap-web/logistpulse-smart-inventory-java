package ec.edu.logistpulse.validacion;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.servicio.SolicitudReserva;
import java.time.LocalDate;

public class ValidarSku extends ManejadorReserva {
    @Override
    protected void validar(SolicitudReserva s, Inventario inventario, LocalDate hoy) {
        if (s.solicitante() == null || s.solicitante().isBlank()) {
            throw new ValidacionException("Debe identificarse el solicitante");
        }
        if (s.sku() == null || s.sku().isBlank() || inventario.buscarProducto(s.sku()) == null) {
            throw new ValidacionException("SKU no registrado: " + s.sku());
        }
        if (s.cantidad() <= 0) {
            throw new ValidacionException("La cantidad debe ser mayor que cero");
        }
    }
}
