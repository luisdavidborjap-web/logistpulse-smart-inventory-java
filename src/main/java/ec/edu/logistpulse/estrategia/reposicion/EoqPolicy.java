package ec.edu.logistpulse.estrategia.reposicion;

/** EOQ: Q* = sqrt(2 · demanda_anual · costo_pedido / costo_mantenimiento_unitario). */
public final class EoqPolicy implements ReplenishmentPolicy {
    public String nombre() { return "EOQ"; }

    public RecomendacionReposicion recomendar(ParametrosReposicion p) {
        int punto = p.puntoReorden();
        double eoq = Math.sqrt(2.0 * p.demandaAnual() * p.costoPorPedido()
                / p.costoMantenimientoAnualUnidad());
        int sugerido = p.stockActual() <= punto ? (int) Math.ceil(eoq) : 0;
        return new RecomendacionReposicion(p.sku(), nombre(), punto, sugerido,
                "Lote económico redondeado hacia arriba; modelo simple con demanda estable.");
    }
}
