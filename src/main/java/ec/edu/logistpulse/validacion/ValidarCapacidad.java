package ec.edu.logistpulse.validacion;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.servicio.SolicitudReserva;
import java.time.LocalDate;

public class ValidarCapacidad extends ManejadorReserva {
    @Override
    protected void validar(SolicitudReserva s, Inventario inventario, LocalDate hoy) {
        int espacio = inventario.almacen().getEspacioLibre();
        if (espacio < s.cantidad()) {
            throw new ValidacionException("Zona de preparación sin capacidad: libre=" + espacio
                    + ", solicitado=" + s.cantidad());
        }
    }
}
