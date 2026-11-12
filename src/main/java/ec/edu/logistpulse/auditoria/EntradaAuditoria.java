package ec.edu.logistpulse.auditoria;

import java.time.Instant;
import java.util.UUID;

public record EntradaAuditoria(Instant fecha, UUID comandoId, String tipo, String actor,
                              boolean exitoso, String detalle) { }
