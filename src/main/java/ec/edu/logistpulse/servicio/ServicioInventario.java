package ec.edu.logistpulse.servicio;

import ec.edu.logistpulse.dominio.*;
import ec.edu.logistpulse.validacion.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Receptor del patrón Command y orquestador de la cadena de validación. */
public class ServicioInventario {
    private final Inventario inventario;
    private final Clock reloj;
    private final ManejadorReserva validaciones;

    public ServicioInventario(Inventario inventario) {
        this(inventario, Clock.systemDefaultZone());
    }

    public ServicioInventario(Inventario inventario, Clock reloj) {
        this.inventario = Objects.requireNonNull(inventario);
        this.reloj = Objects.requireNonNull(reloj);
        ValidarSku sku = new ValidarSku();
        sku.enlazar(new ValidarStock())
                .enlazar(new ValidarLote())
                .enlazar(new ValidarCapacidad());
        this.validaciones = sku;
    }

    public UUID reservar(UUID id, SolicitudReserva solicitud) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(solicitud);
        LocalDate hoy = LocalDate.now(reloj);
        if (inventario.buscarReserva(id) != null) {
            throw new ValidacionException("Este ID de reserva ya existe");
        }
        validaciones.verificar(solicitud, inventario, hoy);
        Lote lote = inventario.buscarLote(solicitud.loteId());
        Reserva reserva = new Reserva(id, solicitud.sku(), solicitud.loteId(),
                solicitud.cantidad(), solicitud.solicitante());
        // Operaciones de demo en memoria; en producción serían una sola transacción SQL.
        lote.reservar(solicitud.cantidad());
        inventario.almacen().ocuparPreparacion(solicitud.cantidad());
        inventario.registrarReserva(reserva);
        return reserva.getId();
    }

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
