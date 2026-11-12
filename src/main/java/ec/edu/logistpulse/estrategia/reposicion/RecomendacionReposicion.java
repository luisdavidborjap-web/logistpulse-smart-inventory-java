package ec.edu.logistpulse.estrategia.reposicion;

/** Resultado informativo: recomendar no crea una orden de compra. */
public record RecomendacionReposicion(String sku, String politica,
                                     int puntoReorden, int cantidadSugerida, String explicacion) {
    public boolean necesitaReposicion() { return cantidadSugerida > 0; }
}
