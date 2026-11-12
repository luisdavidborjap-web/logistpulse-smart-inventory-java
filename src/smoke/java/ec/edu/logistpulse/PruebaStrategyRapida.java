package ec.edu.logistpulse;

import ec.edu.logistpulse.auditoria.LibroAuditoria;
import ec.edu.logistpulse.bootstrap.DatosEjemplo;
import ec.edu.logistpulse.comando.*;
import ec.edu.logistpulse.dominio.*;
import ec.edu.logistpulse.estrategia.picking.*;
import ec.edu.logistpulse.estrategia.reposicion.*;
import ec.edu.logistpulse.servicio.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/** Prueba ejecutable con javac/java, sin Maven ni JUnit. */
public class PruebaStrategyRapida {
    public static void main(String[] args) {
        probarPicking(new FefoPickingPolicy(), "L-200");
        probarPicking(new FifoPickingPolicy(), "L-203");
        probarPicking(new LifoPickingPolicy(), "L-204");
        ServicioReposicion reposicion = new ServicioReposicion();
        var parametros = DatosEjemplo.parametrosReposicion("TEC-001", 3);
        exige(reposicion.recomendar(parametros).cantidadSugerida() == 5, "Punto de reorden");
        reposicion.cambiarPolitica(new MinMaxPolicy());
        exige(reposicion.recomendar(parametros).cantidadSugerida() == 12, "Min-Max");
        reposicion.cambiarPolitica(new EoqPolicy());
        exige(reposicion.recomendar(parametros).cantidadSugerida() == 12, "EOQ");
        System.out.println("OK: 6 pruebas Strategy pasaron (FEFO, FIFO, LIFO, punto, Min-Max, EOQ)");
    }

    private static void probarPicking(PickingPolicy policy, String esperado) {
        Inventario inv = DatosEjemplo.crear();
        ServicioInventario servicio = new ServicioInventario(inv,
                Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC));
        servicio.cambiarPickingPolicy(policy);
        var resultado = new ProcesadorComandos(new LibroAuditoria()).ejecutar(
                new ReservarStockCommand(UUID.randomUUID(),
                        new SolicitudReserva("MED-002", "AUTO", 1, "smoke"), servicio));
        exige(resultado.exitoso(), "Reserva " + policy.nombre());
        exige(inv.buscarReserva(resultado.reservaId()).getLoteId().equals(esperado),
                "Selección " + policy.nombre());
    }

    private static void exige(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError("Falló: " + mensaje);
    }
}
