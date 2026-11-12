package ec.edu.logistpulse.estrategia.reposicion;

/** Strategy: cada política implementa un algoritmo de abastecimiento. */
public interface ReplenishmentPolicy {
    String nombre();
    RecomendacionReposicion recomendar(ParametrosReposicion parametros);
}
