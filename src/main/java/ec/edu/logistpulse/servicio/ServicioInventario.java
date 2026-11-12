package ec.edu.logistpulse.servicio;

import ec.edu.logistpulse.dominio.*;
import ec.edu.logistpulse.estrategia.picking.FefoPickingPolicy;
import ec.edu.logistpulse.estrategia.picking.PickingPolicy;
import ec.edu.logistpulse.validacion.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/** Receptor de Command, orquestador de Chain y contexto de Strategy (picking). */
public class ServicioInventario {
    private final Inventario inventario;
    private final Clock reloj;
    private final ManejadorReserva validaciones;
    private PickingPolicy pickingPolicy;
    private final Map<String, PickingPolicy> pickingPorSku = new HashMap<>();

    public ServicioInventario(Inventario inventario) {
        this(inventario, Clock.systemDefaultZone());
    }

    public ServicioInventario(Inventario inventario, Clock reloj) {
        this.inventario = Objects.requireNonNull(inventario);
        this.reloj = Objects.requireNonNull(reloj);
        this.pickingPolicy = new FefoPickingPolicy();
        ValidarSku sku = new ValidarSku();
        sku.enlazar(new ValidarStock())
                .enlazar(new ValidarLote())
                .enlazar(new ValidarCapacidad());
        this.validaciones = sku;
    }

    public void cambiarPickingPolicy(PickingPolicy nueva) {
        this.pickingPolicy = Objects.requireNonNull(nueva);
    }

    public PickingPolicy pickingPolicy() { return pickingPolicy; }

    public void cambiarPickingPolicyParaSku(String sku, PickingPolicy nueva) {
        if (inventario.buscarProducto(sku) == null) {
            throw new IllegalArgumentException("SKU no registrado: " + sku);
        }
        pickingPorSku.put(sku, Objects.requireNonNull(nueva));
    }

    public PickingPolicy pickingPolicyParaSku(String sku) {
        return pickingPorSku.getOrDefault(sku, pickingPolicy);
    }

    public UUID reservar(UUID id, SolicitudReserva solicitud) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(solicitud);
        LocalDate hoy = LocalDate.now(reloj);
        if (inventario.buscarReserva(id) != null) {
            throw new ValidacionException("Este ID de reserva ya existe");
        }
        // El tercer eslabón selecciona por Strategy solo cuando loteId = AUTO.
        ContextoReserva contexto = new ContextoReserva(solicitud, inventario, hoy, pickingPolicyParaSku(solicitud.sku()));
        validaciones.verificar(contexto);
        Lote lote = contexto.loteSeleccionado();
        Reserva reserva = new Reserva(id, solicitud.sku(), lote.getId(),
                solicitud.cantidad(), solicitud.solicitante());
        // Demo en memoria: en producción debe ser una transacción SQL atómica.
        lote.reservar(solicitud.cantidad());
        inventario.almacen().ocuparPreparacion(solicitud.cantidad());
        inventario.registrarReserva(reserva);
        return reserva.getId();
    }

    public Reserva consultarReserva(UUID id) { return inventario.buscarReserva(id); }

    public void liberar(UUID reservaId) {
        Reserva reserva = inventario.buscarReserva(reservaId);
        if (reserva == null) throw new ValidacionException("Reserva inexistente: " + reservaId);
        if (!reserva.isActiva()) throw new ValidacionException("La reserva ya fue liberada");
        Lote lote = inventario.buscarLote(reserva.getLoteId());
        lote.liberar(reserva.getCantidad());
        inventario.almacen().liberarPreparacion(reserva.getCantidad());
        reserva.marcarLiberada();
    }
}
