package ec.edu.logistpulse.comando;

import ec.edu.logistpulse.servicio.ServicioInventario;
import ec.edu.logistpulse.servicio.SolicitudReserva;
import java.util.Objects;
import java.util.UUID;

public class ReservarStockCommand implements Comando {
    private final UUID id;
    private final SolicitudReserva solicitud;
    private final ServicioInventario receptor;

    public ReservarStockCommand(UUID id, SolicitudReserva solicitud, ServicioInventario receptor) {
        this.id = Objects.requireNonNull(id);
        this.solicitud = Objects.requireNonNull(solicitud);
        this.receptor = Objects.requireNonNull(receptor);
    }

    public UUID id() { return id; }
    public String tipo() { return "RESERVAR"; }
    public String actor() { return solicitud.solicitante(); }
    public String huella() {
        return tipo() + "|" + solicitud.sku() + "|" + solicitud.loteId() + "|"
                + solicitud.cantidad() + "|" + solicitud.solicitante();
    }

    public ResultadoComando execute() {
        UUID reservaId = receptor.reservar(id, solicitud);
        return new ResultadoComando(id, true, "Reserva aprobada: " + reservaId, reservaId);
    }
}
