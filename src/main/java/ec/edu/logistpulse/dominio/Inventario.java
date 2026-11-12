package ec.edu.logistpulse.dominio;

import java.time.LocalDate;
import java.util.*;

public class Inventario {
    private final Map<String, Producto> productos = new LinkedHashMap<>();
    private final Map<String, Lote> lotes = new LinkedHashMap<>();
    private final Map<UUID, Reserva> reservas = new LinkedHashMap<>();
    private final Almacen almacen;

    public Inventario(Almacen almacen) { this.almacen = Objects.requireNonNull(almacen); }

    public void agregarProducto(Producto producto) {
        Objects.requireNonNull(producto);
        if (productos.putIfAbsent(producto.sku(), producto) != null) {
            throw new IllegalArgumentException("SKU duplicado: " + producto.sku());
        }
    }

    public void agregarLote(Lote lote) {
        Objects.requireNonNull(lote);
        if (!productos.containsKey(lote.getSku())) {
            throw new IllegalArgumentException("El lote debe pertenecer a un SKU registrado");
        }
        if (lotes.putIfAbsent(lote.getId(), lote) != null) {
            throw new IllegalArgumentException("Lote duplicado: " + lote.getId());
        }
    }

    public Producto buscarProducto(String sku) { return productos.get(sku); }
    public Lote buscarLote(String loteId) { return lotes.get(loteId); }
    public Reserva buscarReserva(UUID id) { return reservas.get(id); }
    public Collection<Producto> productos() { return Collections.unmodifiableCollection(productos.values()); }
    public Collection<Lote> lotes() { return Collections.unmodifiableCollection(lotes.values()); }
    public Collection<Reserva> reservas() { return Collections.unmodifiableCollection(reservas.values()); }
    public Almacen almacen() { return almacen; }

    public int stockReservable(String sku, LocalDate fecha) {
        return lotes.values().stream()
                .filter(l -> l.getSku().equals(sku))
                .filter(l -> !l.isEnCuarentena() && !l.estaVencido(fecha))
                .mapToInt(Lote::getDisponible).sum();
    }

    public void registrarReserva(Reserva reserva) {
        if (reservas.putIfAbsent(reserva.getId(), reserva) != null) {
            throw new IllegalArgumentException("Reserva duplicada");
        }
    }
}
