package ec.edu.logistpulse;

import ec.edu.logistpulse.auditoria.LibroAuditoria;
import ec.edu.logistpulse.comando.*;
import ec.edu.logistpulse.dominio.*;
import ec.edu.logistpulse.servicio.*;
import java.time.*;
import java.util.UUID;

/** Prueba sin dependencias externas para quienes no tengan Maven instalado. */
public class PruebaRapida {
    public static void main(String[] args) {
        Inventario inv = new Inventario(new Almacen(4));
        inv.agregarProducto(new Producto("SKU-1", "Demo"));
        inv.agregarLote(new Lote("OK", "SKU-1", 10, LocalDate.of(2099, 1, 1), false));
        inv.agregarLote(new Lote("CAD", "SKU-1", 1, LocalDate.of(2020, 1, 1), false));
        ServicioInventario servicio = new ServicioInventario(inv);
        LibroAuditoria ledger = new LibroAuditoria();
        ProcesadorComandos bus = new ProcesadorComandos(ledger);
        UUID id = UUID.randomUUID();
        var req = new SolicitudReserva("SKU-1", "OK", 2, "smoke");
        var primera = bus.ejecutar(new ReservarStockCommand(id, req, servicio));
        exige(primera.exitoso() && inv.buscarLote("OK").getDisponible() == 8, "Reserva");
        var repetida = bus.ejecutar(new ReservarStockCommand(id, req, servicio));
        exige(primera.equals(repetida) && ledger.consultar().size() == 1, "Idempotencia");
        var cad = bus.ejecutar(new ReservarStockCommand(UUID.randomUUID(),
                new SolicitudReserva("SKU-1", "CAD", 1, "smoke"), servicio));
        exige(!cad.exitoso() && cad.mensaje().contains("vencido"), "Caducidad");
        var exceso = bus.ejecutar(new ReservarStockCommand(UUID.randomUUID(),
                new SolicitudReserva("SKU-1", "OK", 3, "smoke"), servicio));
        exige(!exceso.exitoso() && exceso.mensaje().contains("capacidad"), "Capacidad");
        var liberada = bus.ejecutar(new LiberarStockCommand(UUID.randomUUID(), primera.reservaId(), "smoke", servicio));
        exige(liberada.exitoso() && inv.buscarLote("OK").getDisponible() == 10, "Liberación");
        exige(ledger.consultar().size() == 4, "Auditoría");
        System.out.println("OK: 6 verificaciones pasaron (reserva, idempotencia, vencimiento, capacidad, liberación y auditoría)");
    }
    private static void exige(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError("Falló: " + mensaje);
    }
}
