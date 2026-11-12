package ec.edu.logistpulse;

import ec.edu.logistpulse.auditoria.LibroAuditoria;
import ec.edu.logistpulse.bootstrap.DatosEjemplo;
import ec.edu.logistpulse.comando.*;
import ec.edu.logistpulse.dominio.*;
import ec.edu.logistpulse.estrategia.picking.*;
import ec.edu.logistpulse.estrategia.reposicion.*;
import ec.edu.logistpulse.servicio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class StrategyTest {
    private static final Clock RELOJ = Clock.fixed(
            Instant.parse("2026-11-12T13:00:00Z"), ZoneOffset.UTC);
    private Inventario inventario;
    private ServicioInventario servicio;
    private ProcesadorComandos procesador;
    private LibroAuditoria auditoria;

    @BeforeEach
    void inicializar() {
        inventario = DatosEjemplo.crear();
        servicio = new ServicioInventario(inventario, RELOJ);
        auditoria = new LibroAuditoria();
        procesador = new ProcesadorComandos(auditoria);
    }

    private ResultadoComando auto(String sku, int cantidad) {
        return procesador.ejecutar(new ReservarStockCommand(UUID.randomUUID(),
                new SolicitudReserva(sku, "AUTO", cantidad, "test"), servicio));
    }

    private String lote(ResultadoComando r) {
        assertTrue(r.exitoso(), r.mensaje());
        return inventario.buscarReserva(r.reservaId()).getLoteId();
    }

    @Test void fefoSeleccionaVencimientoMasProximo() {
        assertEquals("L-200", lote(auto("MED-002", 1)));
    }

    @Test void fifoSeleccionaFechaRecepcionMasAntigua() {
        servicio.cambiarPickingPolicy(new FifoPickingPolicy());
        assertEquals("L-203", lote(auto("MED-002", 1)));
    }

    @Test void lifoSeleccionaFechaRecepcionMasNueva() {
        servicio.cambiarPickingPolicy(new LifoPickingPolicy());
        assertEquals("L-204", lote(auto("MED-002", 1)));
    }

    @Test void cambiarEstrategiaAfectaReservasPosteriores() {
        assertEquals("L-200", lote(auto("MED-002", 1)));
        servicio.cambiarPickingPolicy(new FifoPickingPolicy());
        assertEquals("L-203", lote(auto("MED-002", 1)));
        servicio.cambiarPickingPolicy(new LifoPickingPolicy());
        assertEquals("L-204", lote(auto("MED-002", 1)));
    }

    @Test void reservaManualIgnoraPoliticaAuto() {
        servicio.cambiarPickingPolicy(new FifoPickingPolicy());
        var resultado = procesador.ejecutar(new ReservarStockCommand(UUID.randomUUID(),
                new SolicitudReserva("MED-002", "L-200", 2, "test"), servicio));
        assertEquals("L-200", lote(resultado));
    }

    @Test void autoNoEligeLoteVencidoNiCuarentena() {
        // Aunque tengan fecha de recepción antigua, se descartan.
        servicio.cambiarPickingPolicy(new FifoPickingPolicy());
        assertEquals("L-203", lote(auto("MED-002", 1)));
        assertEquals(5, inventario.buscarLote("L-201").getDisponible());
        assertEquals(4, inventario.buscarLote("L-202").getDisponible());
    }

    @Test void autoRechazaCuandoNingunLoteTieneTodaLaCantidad() {
        Inventario separado = new Inventario(new Almacen(20));
        separado.agregarProducto(new Producto("P", "Prueba"));
        separado.agregarLote(new Lote("X", "P", 4, LocalDate.of(2029, 1, 1), false,
                LocalDate.of(2026, 1, 1)));
        separado.agregarLote(new Lote("Y", "P", 4, LocalDate.of(2030, 1, 1), false,
                LocalDate.of(2026, 2, 1)));
        ServicioInventario aislado = new ServicioInventario(separado, RELOJ);
        var r = procesador.ejecutar(new ReservarStockCommand(UUID.randomUUID(),
                new SolicitudReserva("P", "AUTO", 6, "test"), aislado));
        assertFalse(r.exitoso());
        assertTrue(r.mensaje().contains("no se divide entre lotes"));
        assertEquals(0, separado.almacen().getOcupacionPreparacion());
        assertEquals(4, separado.buscarLote("X").getDisponible());
    }

    @Test void autoRespetaCapacidadSinDescontarUnidades() {
        var r = auto("MED-001", 9);
        assertFalse(r.exitoso());
        assertTrue(r.mensaje().contains("capacidad"));
        assertEquals(10, inventario.buscarLote("L-100").getDisponible());
    }

    @Test void autoLiberaExactamenteLoteAsignado() {
        servicio.cambiarPickingPolicy(new LifoPickingPolicy());
        var r = auto("MED-002", 3);
        assertEquals("L-204", lote(r));
        var liberacion = procesador.ejecutar(new LiberarStockCommand(UUID.randomUUID(),
                r.reservaId(), "test", servicio));
        assertTrue(liberacion.exitoso());
        assertEquals(7, inventario.buscarLote("L-204").getDisponible());
        assertEquals(0, inventario.almacen().getOcupacionPreparacion());
    }

    @Test void mismaReservaAutoEsIdempotenteTrasCambioDePolitica() {
        UUID id = UUID.randomUUID();
        var req = new SolicitudReserva("MED-002", "AUTO", 2, "test");
        var primera = procesador.ejecutar(new ReservarStockCommand(id, req, servicio));
        servicio.cambiarPickingPolicy(new FifoPickingPolicy());
        var repetida = procesador.ejecutar(new ReservarStockCommand(id, req, servicio));
        assertEquals(primera, repetida);
        assertEquals("L-200", lote(repetida));
        assertEquals(4, inventario.buscarLote("L-200").getDisponible());
        assertEquals(1, auditoria.consultar().size());
    }

    @Test void autoValidaSkuYCantidadAntesDeElegir() {
        assertTrue(auto("NO-EXISTE", 1).mensaje().contains("SKU no registrado"));
        assertTrue(auto("MED-002", 0).mensaje().contains("cantidad"));
    }

    @Test void autoSeleccionaSiguienteLoteSiPrimeroNoAlcanza() {
        assertEquals("L-203", lote(auto("MED-002", 7)));
    }

    @Test void sePuedeConfigurarPickingPorProducto() {
        servicio.cambiarPickingPolicyParaSku("MED-002", new FifoPickingPolicy());
        assertEquals("L-203", lote(auto("MED-002", 1)));
        assertEquals("FEFO", servicio.pickingPolicyParaSku("MED-001").nombre());
    }

    @Test void pickingPorSkuTienePrioridadSobrePoliticaGeneral() {
        servicio.cambiarPickingPolicyParaSku("MED-002", new LifoPickingPolicy());
        servicio.cambiarPickingPolicy(new FifoPickingPolicy());
        assertEquals("L-204", lote(auto("MED-002", 1)));
    }

    private ParametrosReposicion parametrosTec() {
        return DatosEjemplo.parametrosReposicion("TEC-001", 3);
    }

    @Test void puntoDeReordenCalculaUmbralYDemanda() {
        var r = new PuntoReordenPolicy().recomendar(parametrosTec());
        assertEquals(7, r.puntoReorden());
        assertEquals(5, r.cantidadSugerida());
    }

    @Test void minMaxReponeHastaMaximo() {
        var r = new MinMaxPolicy().recomendar(parametrosTec());
        assertEquals(12, r.cantidadSugerida());
    }

    @Test void eoqAplicaFormulaRaizCuadrada() {
        var r = new EoqPolicy().recomendar(parametrosTec());
        assertEquals(12, r.cantidadSugerida()); // ceil(sqrt(2*365*2/12)) = 12
    }

    @Test void politicaDeReposicionSeCambiaEnEjecucion() {
        ServicioReposicion contexto = new ServicioReposicion();
        assertEquals(5, contexto.recomendar(parametrosTec()).cantidadSugerida());
        contexto.cambiarPolitica(new MinMaxPolicy());
        assertEquals(12, contexto.recomendar(parametrosTec()).cantidadSugerida());
    }

    @Test void reposicionPorSkuTienePrioridadSobrePoliticaGeneral() {
        ServicioReposicion contexto = new ServicioReposicion();
        contexto.cambiarPoliticaParaSku("TEC-001", new MinMaxPolicy());
        contexto.cambiarPolitica(new EoqPolicy());
        assertEquals("MIN-MAX", contexto.recomendar(parametrosTec()).politica());
        var otro = DatosEjemplo.parametrosReposicion("MED-001", 2);
        assertEquals("EOQ", contexto.recomendar(otro).politica());
    }

    @Test void noSeReponeCuandoStockSuperaPunto() {
        var p = DatosEjemplo.parametrosReposicion("TEC-001", 8);
        assertEquals(0, new PuntoReordenPolicy().recomendar(p).cantidadSugerida());
        assertEquals(0, new MinMaxPolicy().recomendar(p).cantidadSugerida());
        assertEquals(0, new EoqPolicy().recomendar(p).cantidadSugerida());
    }

    @Test void reposicionNoModificaInventario() {
        int antes = inventario.buscarLote("L-300").getDisponible();
        ServicioReposicion contexto = new ServicioReposicion();
        contexto.recomendar(parametrosTec());
        assertEquals(antes, inventario.buscarLote("L-300").getDisponible());
    }

    @Test void parametrosInvalidosSeRechazan() {
        assertThrows(IllegalArgumentException.class,
                () -> new ParametrosReposicion("P", 1, 10, 3, 1, 3, 365, 4, 1));
    }
}
