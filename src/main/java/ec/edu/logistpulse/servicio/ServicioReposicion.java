package ec.edu.logistpulse.servicio;

import ec.edu.logistpulse.estrategia.reposicion.*;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

/** Contexto Strategy de reposición: permite intercambiar algoritmos en ejecución. */
public final class ServicioReposicion {
    private ReplenishmentPolicy politica = new PuntoReordenPolicy();
    private final Map<String, ReplenishmentPolicy> politicasPorSku = new HashMap<>();

    public ReplenishmentPolicy politica() { return politica; }

    public void cambiarPolitica(ReplenishmentPolicy nueva) {
        this.politica = Objects.requireNonNull(nueva);
    }

    public void cambiarPoliticaParaSku(String sku, ReplenishmentPolicy nueva) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU requerido");
        politicasPorSku.put(sku, Objects.requireNonNull(nueva));
    }

    public ReplenishmentPolicy politicaParaSku(String sku) {
        return politicasPorSku.getOrDefault(sku, politica);
    }

    public RecomendacionReposicion recomendar(ParametrosReposicion parametros) {
        Objects.requireNonNull(parametros);
        return politicaParaSku(parametros.sku()).recomendar(parametros);
    }
}
