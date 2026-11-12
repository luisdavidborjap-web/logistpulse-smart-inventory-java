package ec.edu.logistpulse;

import ec.edu.logistpulse.auditoria.*;
import ec.edu.logistpulse.bootstrap.DatosEjemplo;
import ec.edu.logistpulse.comando.*;
import ec.edu.logistpulse.dominio.*;
import ec.edu.logistpulse.servicio.*;
import java.util.Scanner;
import java.util.UUID;

/** Aplicación de consola para probar los dos patrones sin servidor ni base de datos. */
public final class App {
    private App() { }

    public static void main(String[] args) {
        Inventario inventario = DatosEjemplo.crear();
        ServicioInventario servicio = new ServicioInventario(inventario);
        LibroAuditoria auditoria = new LibroAuditoria();
        ProcesadorComandos procesador = new ProcesadorComandos(auditoria);
        Scanner sc = new Scanner(System.in);

        System.out.println("=== LOGISTPULSE | Smart Inventory ===");
        while (true) {
            System.out.println("\n1. Ver inventario   2. Reservar stock   3. Liberar reserva");
            System.out.println("4. Ver reservas     5. Ver auditoría     0. Salir");
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
                    System.out.printf("  Lote=%s | disponible=%d | reservado=%d | vence=%s | cuarentena=%s%n",
                            l.getId(), l.getDisponible(), l.getReservado(), l.getVencimiento(), l.isEnCuarentena());
                }
            }
        }
    }

    private static void reservar(Scanner sc, ServicioInventario servicio, ProcesadorComandos procesador) {
        System.out.print("SKU: "); String sku = sc.nextLine().trim();
        System.out.print("Lote: "); String lote = sc.nextLine().trim();
        System.out.print("Cantidad: "); int cantidad = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Solicitante: "); String actor = sc.nextLine().trim();
        var solicitud = new SolicitudReserva(sku, lote, cantidad, actor);
        var resultado = procesador.ejecutar(new ReservarStockCommand(UUID.randomUUID(), solicitud, servicio));
        imprimirResultado(resultado);
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
