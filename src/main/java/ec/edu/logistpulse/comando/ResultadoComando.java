package ec.edu.logistpulse.comando;

import java.util.UUID;

public record ResultadoComando(UUID comandoId, boolean exitoso, String mensaje, UUID reservaId) { }
