package ec.edu.logistpulse.dominio;

import java.time.Instant;
import java.util.UUID;

public class Reserva {
    private final UUID id;
    private final String sku;
    private final String loteId;
    private final int cantidad;
    private final String solicitante;
    private final Instant creadaEn;
    private boolean activa = true;

    public Reserva(UUID id, String sku, String loteId, int cantidad, String solicitante) {
        this.id = id;
        this.sku = sku;
        this.loteId = loteId;
        this.cantidad = cantidad;
        this.solicitante = solicitante;
        this.creadaEn = Instant.now();
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getLoteId() { return loteId; }
    public int getCantidad() { return cantidad; }
    public String getSolicitante() { return solicitante; }
    public Instant getCreadaEn() { return creadaEn; }
    public boolean isActiva() { return activa; }

    public void marcarLiberada() {
        if (!activa) throw new IllegalStateException("La reserva ya fue liberada");
        activa = false;
    }
}
