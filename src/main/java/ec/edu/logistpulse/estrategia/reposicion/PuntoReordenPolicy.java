package ec.edu.logistpulse.estrategia.reposicion;

/** Se activa cuando existencias <= consumo durante entrega + seguridad. */
public final class PuntoReordenPolicy implements ReplenishmentPolicy {
    public String nombre() { return "PUNTO DE REORDEN"; }

    public RecomendacionReposicion recomendar(ParametrosReposicion p) {
        int punto = p.puntoReorden();
        int sugerido = p.stockActual() <= punto
                ? Math.max(0, p.demandaDiaria() * p.plazoEntregaDias()) : 0;
        return new RecomendacionReposicion(p.sku(), nombre(), punto, sugerido,
                "Si el stock cae al umbral, sugerir demanda prevista durante el plazo de entrega.");
    }
}
