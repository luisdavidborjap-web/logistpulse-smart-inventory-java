package ec.edu.logistpulse.estrategia.reposicion;

/** Cuando se alcanza el mínimo, se recomienda comprar hasta el máximo. */
public final class MinMaxPolicy implements ReplenishmentPolicy {
    public String nombre() { return "MIN-MAX"; }

    public RecomendacionReposicion recomendar(ParametrosReposicion p) {
        int minimo = p.puntoReorden();
        int sugerido = p.stockActual() <= minimo ? p.stockMaximo() - p.stockActual() : 0;
        return new RecomendacionReposicion(p.sku(), nombre(), minimo, sugerido,
                "Cuando stock <= mínimo, completar hasta " + p.stockMaximo() + " unidades.");
    }
}
