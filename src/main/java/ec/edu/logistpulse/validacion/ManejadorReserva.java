package ec.edu.logistpulse.validacion;

import ec.edu.logistpulse.servicio.ContextoReserva;

/** Chain of Responsibility: cada eslabón verifica y delega en el siguiente. */
public abstract class ManejadorReserva {
    private ManejadorReserva siguiente;

    public ManejadorReserva enlazar(ManejadorReserva siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    public final void verificar(ContextoReserva contexto) {
        validar(contexto);
        if (siguiente != null) siguiente.verificar(contexto);
    }

    protected abstract void validar(ContextoReserva contexto);
}
