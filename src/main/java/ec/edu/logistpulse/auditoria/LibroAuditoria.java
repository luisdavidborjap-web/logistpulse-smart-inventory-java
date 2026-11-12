package ec.edu.logistpulse.auditoria;

import java.util.ArrayList;
import java.util.List;

/** Historial en memoria, demostrativo; reinicia al cerrar el programa. */
public class LibroAuditoria {
    private final List<EntradaAuditoria> entradas = new ArrayList<>();
    public void registrar(EntradaAuditoria entrada) { entradas.add(entrada); }
    public List<EntradaAuditoria> consultar() { return List.copyOf(entradas); }
}
