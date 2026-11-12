package ec.edu.logistpulse;

import ec.edu.logistpulse.auditoria.*;
import ec.edu.logistpulse.bootstrap.DatosEjemplo;
import ec.edu.logistpulse.comando.*;
import ec.edu.logistpulse.dominio.*;
import ec.edu.logistpulse.estrategia.picking.*;
import ec.edu.logistpulse.estrategia.reposicion.*;
import ec.edu.logistpulse.servicio.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

/** Demostración de Command + Chain of Responsibility + Strategy. */
public final class App {
    private App() { }

    public static void main(String[] args) {
        Inventario inventario = DatosEjemplo.crear();
        ServicioInventario servicio = new ServicioInventario(inventario);
        ServicioReposicion reposicion = new ServicioReposicion();
        LibroAuditoria auditoria = new LibroAuditoria();
        ProcesadorComandos procesador = new ProcesadorComandos(auditoria);
        Scanner sc = new Scanner(System.in);

        System.out.println("=== LOGISTPULSE | Smart Inventory ===");
        while (true) {
            System.out.println("\n1. Ver inventario    2. Reservar stock   3. Liberar reserva");
            System.out.println("4. Ver reservas      5. Ver auditoría    6. Cambiar picking");
            System.out.println("7. Cambiar reposición 8. Recomendar reposición 0. Salir");
            System.out.println("Picking: " + servicio.pickingPolicy().nombre()
                    + " | Reposición: " + reposicion.politica().nombre());
            System.out.print("Opción: ");
            if (!sc.hasNextLine()) break;
            String opcion = sc.nextLine().trim();
            try {
                switch (opcion) {
                    case "1" -> verInventario(inventario);
                    case "2" -> reservar(sc, servicio, procesador);
                    case "3" -> liberar(sc, servicio, procesador);
                    case "4" -> verReservas(inventario);
                    case "5" -> verAuditoria(auditoria);
                    case "6" -> cambiarPicking(sc, servicio);
                    case "7" -> cambiarReposicion(sc, reposicion);
                    case "8" -> recomendar(sc, inventario, reposicion);
                    case "0" -> { System.out.println("Hasta luego."); return; }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Entrada inválida: " + ex.getMessage());
            }
        }
    }

    private static void verInventario(Inventario inv) {
        System.out.printf("Preparación: %d/%d unidades ocupadas%n",
                inv.almacen().getOcupacionPreparacion(), inv.almacen().getCapacidadPreparacion());
        for (Producto p : inv.productos()) {
            System.out.println(p.sku() + " - " + p.nombre());
            for (Lote l : inv.lotes()) {
                if (l.getSku().equals(p.sku())) {
                    System.out.printf("  Lote=%s | disponible=%d | reservado=%d | recepción=%s | vence=%s | cuarentena=%s%n",
                            l.getId(), l.getDisponible(), l.getReservado(), l.getFechaRecepcion(),
                            l.getVencimiento(), l.isEnCuarentena());
                }
            }
        }
    }

    private static void reservar(Scanner sc, ServicioInventario servicio, ProcesadorComandos procesador) {
        System.out.print("SKU: "); String sku = sc.nextLine().trim();
        System.out.print("Lote (ID o AUTO para usar Strategy): "); String lote = sc.nextLine().trim();
        System.out.print("Cantidad: "); int cantidad = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Solicitante: "); String actor = sc.nextLine().trim();
        var solicitud = new SolicitudReserva(sku, lote, cantidad, actor);
        var resultado = procesador.ejecutar(new ReservarStockCommand(UUID.randomUUID(), solicitud, servicio));
        imprimirResultado(resultado);
    }

    private static void cambiarPicking(Scanner sc, ServicioInventario servicio) {
        System.out.print("Selecciona 1=FEFO  2=FIFO  3=LIFO: ");
        PickingPolicy politica = switch (sc.nextLine().trim()) {
            case "1" -> new FefoPickingPolicy();
            case "2" -> new FifoPickingPolicy();
            case "3" -> new LifoPickingPolicy();
            default -> throw new IllegalArgumentException("Política de picking desconocida");
        };
        System.out.print("¿Aplicar a TODOS o solo a un SKU (ej. MED-002)?: ");
        String destino = sc.nextLine().trim();
        if (destino.equalsIgnoreCase("TODOS") || destino.isBlank()) {
            servicio.cambiarPickingPolicy(politica);
            System.out.println("Picking general: " + politica.nombre());
        } else {
            servicio.cambiarPickingPolicyParaSku(destino, politica);
            System.out.println("Picking para " + destino + ": " + politica.nombre());
        }
    }

    private static void cambiarReposicion(Scanner sc, ServicioReposicion servicio) {
        System.out.print("Selecciona 1=Punto de reorden  2=Min-Max  3=EOQ: ");
        ReplenishmentPolicy politica = switch (sc.nextLine().trim()) {
            case "1" -> new PuntoReordenPolicy();
            case "2" -> new MinMaxPolicy();
            case "3" -> new EoqPolicy();
            default -> throw new IllegalArgumentException("Política de reposición desconocida");
        };
        System.out.print("¿Aplicar a TODOS o solo a un SKU (ej. TEC-001)?: ");
        String destino = sc.nextLine().trim();
        if (destino.equalsIgnoreCase("TODOS") || destino.isBlank()) {
            servicio.cambiarPolitica(politica);
            System.out.println("Reposición general: " + politica.nombre());
        } else {
            servicio.cambiarPoliticaParaSku(destino, politica);
            System.out.println("Reposición para " + destino + ": " + politica.nombre());
        }
    }

    private static void recomendar(Scanner sc, Inventario inventario, ServicioReposicion servicio) {
        System.out.print("SKU para recomendar reposición (MED-001, MED-002, TEC-001): ");
        String sku = sc.nextLine().trim();
        if (inventario.buscarProducto(sku) == null) {
            throw new IllegalArgumentException("SKU no registrado: " + sku);
        }
        int stock = inventario.stockReservable(sku, LocalDate.now());
        ParametrosReposicion parametros = DatosEjemplo.parametrosReposicion(sku, stock);
        RecomendacionReposicion r = servicio.recomendar(parametros);
        System.out.printf("Política=%s | SKU=%s | stock=%d | punto de reorden=%d | sugerido=%d unidades%n",
                r.politica(), r.sku(), stock, r.puntoReorden(), r.cantidadSugerida());
        System.out.println(r.necesitaReposicion() ? "SE RECOMIENDA REPONER" : "No se requiere reposición");
        System.out.println(r.explicacion());
        System.out.println("Nota: es una simulación y NO registra compras.");
    }

    private static void liberar(Scanner sc, ServicioInventario servicio, ProcesadorComandos procesador) {
        System.out.print("ID de reserva: "); UUID reservaId = UUID.fromString(sc.nextLine().trim());
        System.out.print("Solicitante: "); String actor = sc.nextLine().trim();
        var resultado = procesador.ejecutar(new LiberarStockCommand(UUID.randomUUID(), reservaId, actor, servicio));
        imprimirResultado(resultado);
    }

    private static void verReservas(Inventario inv) {
        if (inv.reservas().isEmpty()) { System.out.println("Aún no hay reservas."); return; }
        for (Reserva r : inv.reservas()) {
            System.out.printf("ID=%s | SKU=%s | lote=%s | unidades=%d | actor=%s | estado=%s%n",
                    r.getId(), r.getSku(), r.getLoteId(), r.getCantidad(), r.getSolicitante(),
                    r.isActiva() ? "ACTIVA" : "LIBERADA");
        }
    }

    private static void verAuditoria(LibroAuditoria auditoria) {
        if (auditoria.consultar().isEmpty()) { System.out.println("Aún no hay eventos."); return; }
        for (EntradaAuditoria e : auditoria.consultar()) {
            System.out.printf("%s | %s | %s | actor=%s | %s | %s%n", e.fecha(), e.comandoId(),
                    e.tipo(), e.actor(), e.exitoso() ? "APROBADO" : "RECHAZADO", e.detalle());
        }
    }

    private static void imprimirResultado(ResultadoComando resultado) {
        System.out.println((resultado.exitoso() ? "[OK] " : "[ERROR] ") + resultado.mensaje());
    }
}
