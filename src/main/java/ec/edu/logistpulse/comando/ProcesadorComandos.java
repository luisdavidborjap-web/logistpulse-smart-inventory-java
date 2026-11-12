package ec.edu.logistpulse.comando;

import ec.edu.logistpulse.auditoria.EntradaAuditoria;
import ec.edu.logistpulse.auditoria.LibroAuditoria;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Invocador Command: aplica idempotencia y registra todas las decisiones nuevas. */
public class ProcesadorComandos {
    private record Procesado(String huella, ResultadoComando resultado) { }
    private final Map<UUID, Procesado> ejecutados = new HashMap<>();
    private final LibroAuditoria auditoria;

    public ProcesadorComandos(LibroAuditoria auditoria) {
        this.auditoria = Objects.requireNonNull(auditoria);
    }

    // Serializa operaciones de esta instancia. No sustituye transacciones de base de datos.
    public synchronized ResultadoComando ejecutar(Comando comando) {
        Objects.requireNonNull(comando);
        Procesado previo = ejecutados.get(comando.id());
        if (previo != null) {
            if (previo.huella().equals(comando.huella())) return previo.resultado();
            String mensaje = "ID de comando reutilizado para una operación diferente";
            auditoria.registrar(new EntradaAuditoria(Instant.now(), comando.id(), comando.tipo(),
                    comando.actor(), false, mensaje));
            return new ResultadoComando(comando.id(), false, mensaje, null);
        }

        ResultadoComando resultado;
        try {
            resultado = comando.execute();
        } catch (RuntimeException ex) {
            resultado = new ResultadoComando(comando.id(), false, ex.getMessage(), null);
        }
        ejecutados.put(comando.id(), new Procesado(comando.huella(), resultado));
        auditoria.registrar(new EntradaAuditoria(Instant.now(), comando.id(), comando.tipo(),
                comando.actor(), resultado.exitoso(), resultado.mensaje()));
        return resultado;
    }
}
