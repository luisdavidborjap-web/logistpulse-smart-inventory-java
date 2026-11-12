package ec.edu.logistpulse.comando;

import ec.edu.logistpulse.servicio.ServicioInventario;
import java.util.Objects;
import java.util.UUID;

public class LiberarStockCommand implements Comando {
    private final UUID id;
    private final UUID reservaId;
    private final String actor;
    private final ServicioInventario receptor;

    public LiberarStockCommand(UUID id, UUID reservaId, String actor, ServicioInventario receptor) {
        this.id = Objects.requireNonNull(id);
        this.reservaId = Objects.requireNonNull(reservaId);
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("El solicitante es obligatorio");
        }
        this.actor = actor;
        this.receptor = Objects.requireNonNull(receptor);
    }

    public UUID id() { return id; }
    public String tipo() { return "LIBERAR"; }
    public String actor() { return actor; }
    public String huella() { return tipo() + "|" + reservaId + "|" + actor; }
    public ResultadoComando execute() {
        receptor.liberar(reservaId);
        return new ResultadoComando(id, true, "Reserva liberada: " + reservaId, reservaId);
    }
}
