package ec.edu.logistpulse.validacion;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.servicio.SolicitudReserva;
import java.time.LocalDate;

/** Patrón Chain of Responsibility: cada eslabón verifica y delega. */
public abstract class ManejadorReserva {
    private ManejadorReserva siguiente;

    public ManejadorReserva enlazar(ManejadorReserva siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    public final void verificar(SolicitudReserva solicitud, Inventario inventario, LocalDate hoy) {
        validar(solicitud, inventario, hoy);
        if (siguiente != null) siguiente.verificar(solicitud, inventario, hoy);
    }

    protected abstract void validar(SolicitudReserva solicitud, Inventario inventario, LocalDate hoy);
}
