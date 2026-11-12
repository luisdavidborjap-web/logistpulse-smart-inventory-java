package ec.edu.logistpulse;

import ec.edu.logistpulse.auditoria.LibroAuditoria;
import ec.edu.logistpulse.comando.*;
import ec.edu.logistpulse.dominio.*;
import ec.edu.logistpulse.servicio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SistemaTest {
    private Inventario inventario;
    private ServicioInventario servicio;
    private ProcesadorComandos bus;
    private LibroAuditoria ledger;

    @BeforeEach
    void preparar() {
        inventario = new Inventario(new Almacen(5));
        inventario.agregarProducto(new Producto("SKU-1", "Producto 1"));
        inventario.agregarProducto(new Producto("SKU-2", "Producto 2"));
        inventario.agregarLote(new Lote("OK", "SKU-1", 10, LocalDate.of(2027, 1, 1), false));
        inventario.agregarLote(new Lote("CUARENTENA", "SKU-1", 5, LocalDate.of(2027, 1, 1), true));
        inventario.agregarLote(new Lote("VENCIDO", "SKU-1", 5, LocalDate.of(2020, 1, 1), false));
        inventario.agregarLote(new Lote("OTRO", "SKU-2", 5, LocalDate.of(2027, 1, 1), false));
        servicio = new ServicioInventario(inventario, Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC));
        ledger = new LibroAuditoria();
        bus = new ProcesadorComandos(ledger);
    }

    private ResultadoComando reservar(String sku, String lote, int cantidad) {
        return bus.ejecutar(new ReservarStockCommand(UUID.randomUUID(),
                new SolicitudReserva(sku, lote, cantidad, "pruebas"), servicio));
    }

    @Test void reservaExitosaActualizaLoteYCapacidad() {
        var r = reservar("SKU-1", "OK", 3);
        assertTrue(r.exitoso());
        assertNotNull(r.reservaId());
        assertEquals(7, inventario.buscarLote("OK").getDisponible());
        assertEquals(3, inventario.buscarLote("OK").getReservado());
        assertEquals(3, inventario.almacen().getOcupacionPreparacion());
        assertEquals(1, ledger.consultar().size());
    }

    @Test void rechazaSkuInexistente() {
        assertTrue(reservar("MAL", "OK", 1).mensaje().contains("SKU no registrado"));
    }

    @Test void rechazaCantidadNoPositiva() {
        assertTrue(reservar("SKU-1", "OK", 0).mensaje().contains("cantidad"));
    }

    @Test void rechazaStockInsuficiente() {
        var r = reservar("SKU-1", "OK", 11);
        assertFalse(r.exitoso());
        assertTrue(r.mensaje().contains("Stock insuficiente"));
    }

    @Test void rechazaLoteVencido() {
        assertTrue(reservar("SKU-1", "VENCIDO", 1).mensaje().contains("vencido"));
    }

    @Test void rechazaLoteEnCuarentena() {
        assertTrue(reservar("SKU-1", "CUARENTENA", 1).mensaje().contains("cuarentena"));
    }

    @Test void rechazaLoteQuePerteneceAOtroSku() {
        assertTrue(reservar("SKU-1", "OTRO", 1).mensaje().contains("no pertenece"));
    }

    @Test void rechazaCapacidadInsuficiente() {
        var r = reservar("SKU-1", "OK", 6);
        assertFalse(r.exitoso());
        assertTrue(r.mensaje().contains("capacidad"));
        assertEquals(10, inventario.buscarLote("OK").getDisponible());
    }

    @Test void mismoComandoEsIdempotenteInclusoEnAuditoria() {
        UUID id = UUID.randomUUID();
        var solicitud = new SolicitudReserva("SKU-1", "OK", 2, "pruebas");
        var primera = bus.ejecutar(new ReservarStockCommand(id, solicitud, servicio));
        var segunda = bus.ejecutar(new ReservarStockCommand(id, solicitud, servicio));
        assertEquals(primera, segunda);
        assertEquals(8, inventario.buscarLote("OK").getDisponible());
        assertEquals(1, ledger.consultar().size());
    }

    @Test void liberarCompensaReservaAnterior() {
        var r = reservar("SKU-1", "OK", 3);
        var liberacion = bus.ejecutar(new LiberarStockCommand(UUID.randomUUID(), r.reservaId(), "pruebas", servicio));
        assertTrue(liberacion.exitoso());
        assertEquals(10, inventario.buscarLote("OK").getDisponible());
        assertEquals(0, inventario.almacen().getOcupacionPreparacion());
        assertFalse(inventario.buscarReserva(r.reservaId()).isActiva());
        assertEquals(2, ledger.consultar().size());
    }

    @Test void segundaLiberacionConIdDistintoEsRechazada() {
        UUID reservaId = reservar("SKU-1", "OK", 2).reservaId();
        bus.ejecutar(new LiberarStockCommand(UUID.randomUUID(), reservaId, "pruebas", servicio));
        var segunda = bus.ejecutar(new LiberarStockCommand(UUID.randomUUID(), reservaId, "pruebas", servicio));
        assertFalse(segunda.exitoso());
        assertTrue(segunda.mensaje().contains("ya fue liberada"));
    }

    @Test void llaveReutilizadaConOtroContenidoEsRechazada() {
        UUID id = UUID.randomUUID();
        bus.ejecutar(new ReservarStockCommand(id, new SolicitudReserva("SKU-1", "OK", 1, "pruebas"), servicio));
        var r = bus.ejecutar(new ReservarStockCommand(id, new SolicitudReserva("SKU-1", "OK", 2, "pruebas"), servicio));
        assertFalse(r.exitoso());
        assertEquals(9, inventario.buscarLote("OK").getDisponible());
    }
}
