package ec.edu.logistpulse.servicio;

import ec.edu.logistpulse.dominio.Inventario;
import ec.edu.logistpulse.dominio.Lote;
import ec.edu.logistpulse.estrategia.picking.PickingPolicy;
import java.time.LocalDate;
import java.util.Objects;

/** Datos compartidos en Chain; el manejador de lote resuelve el picking AUTO. */
public final class ContextoReserva {
    private final SolicitudReserva solicitud;
    private final Inventario inventario;
    private final LocalDate hoy;
    private final PickingPolicy pickingPolicy;
    private Lote loteSeleccionado;

    public ContextoReserva(SolicitudReserva solicitud, Inventario inventario,
                           LocalDate hoy, PickingPolicy pickingPolicy) {
        this.solicitud = Objects.requireNonNull(solicitud);
        this.inventario = Objects.requireNonNull(inventario);
        this.hoy = Objects.requireNonNull(hoy);
        this.pickingPolicy = Objects.requireNonNull(pickingPolicy);
    }

    public SolicitudReserva solicitud() { return solicitud; }
    public Inventario inventario() { return inventario; }
    public LocalDate hoy() { return hoy; }
    public PickingPolicy pickingPolicy() { return pickingPolicy; }
    public Lote loteSeleccionado() { return loteSeleccionado; }

    public void seleccionarLote(Lote lote) { this.loteSeleccionado = Objects.requireNonNull(lote); }
}
