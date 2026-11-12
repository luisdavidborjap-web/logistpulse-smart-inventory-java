package ec.edu.logistpulse.comando;

import java.util.UUID;

/** Patrón Command: todas las acciones exponen execute(). */
public interface Comando {
    UUID id();
    String tipo();
    String actor();
    String huella();
    ResultadoComando execute();
}
