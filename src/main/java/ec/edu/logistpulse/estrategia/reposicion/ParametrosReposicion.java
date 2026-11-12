package ec.edu.logistpulse.estrategia.reposicion;

/** Parámetros simplificados de inventario y demanda para simulación académica. */
public record ParametrosReposicion(
        String sku, int stockActual, int demandaDiaria, int plazoEntregaDias,
        int stockSeguridad, int stockMaximo, int demandaAnual,
        double costoPorPedido, double costoMantenimientoAnualUnidad) {

    public ParametrosReposicion {
        if (sku == null || sku.isBlank() || stockActual < 0 || demandaDiaria < 0
                || plazoEntregaDias < 0 || stockSeguridad < 0 || stockMaximo < 0
                || demandaAnual < 0 || costoPorPedido <= 0
                || costoMantenimientoAnualUnidad <= 0) {
            throw new IllegalArgumentException("Parámetros de reposición no válidos");
        }
        if (stockMaximo < ((long) demandaDiaria * plazoEntregaDias + stockSeguridad)) {
            throw new IllegalArgumentException("El nivel máximo debe ser al menos el punto de reorden");
        }
    }

    public int puntoReorden() {
        return Math.addExact(Math.multiplyExact(demandaDiaria, plazoEntregaDias), stockSeguridad);
    }
}
