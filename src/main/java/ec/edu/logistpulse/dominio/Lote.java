package ec.edu.logistpulse.dominio;

import java.time.LocalDate;
import java.util.Objects;

public class Lote {
    private final String id;
    private final String sku;
    private final LocalDate vencimiento;
    private final boolean enCuarentena;
    private int disponible;
    private int reservado;

    public Lote(String id, String sku, int disponible, LocalDate vencimiento, boolean enCuarentena) {
        if (id == null || id.isBlank() || sku == null || sku.isBlank() || disponible < 0) {
            throw new IllegalArgumentException("Datos de lote no válidos");
        }
        this.id = id;
        this.sku = sku;
        this.disponible = disponible;
        this.vencimiento = Objects.requireNonNull(vencimiento, "El vencimiento es obligatorio");
        this.enCuarentena = enCuarentena;
    }

    public String getId() { return id; }
    public String getSku() { return sku; }
    public int getDisponible() { return disponible; }
    public int getReservado() { return reservado; }
    public LocalDate getVencimiento() { return vencimiento; }
    public boolean isEnCuarentena() { return enCuarentena; }
    public boolean estaVencido(LocalDate fecha) { return vencimiento.isBefore(fecha); }

    public void reservar(int cantidad) {
        if (cantidad <= 0 || disponible < cantidad) {
            throw new IllegalStateException("No hay unidades suficientes en el lote " + id);
        }
        disponible -= cantidad;
        reservado += cantidad;
    }

    public void liberar(int cantidad) {
        if (cantidad <= 0 || reservado < cantidad) {
            throw new IllegalStateException("No es posible liberar esa cantidad del lote " + id);
        }
        reservado -= cantidad;
        disponible += cantidad;
    }
}
