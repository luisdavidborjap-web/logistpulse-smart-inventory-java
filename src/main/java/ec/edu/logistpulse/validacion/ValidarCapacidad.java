package ec.edu.logistpulse.validacion;

import ec.edu.logistpulse.servicio.ContextoReserva;

public class ValidarCapacidad extends ManejadorReserva {
    @Override
    protected void validar(ContextoReserva c) {
        int espacio = c.inventario().almacen().getEspacioLibre();
        if (espacio < c.solicitud().cantidad()) {
            throw new ValidacionException("Zona de preparación sin capacidad: libre=" + espacio
                    + ", solicitado=" + c.solicitud().cantidad());
        }
    }
}
