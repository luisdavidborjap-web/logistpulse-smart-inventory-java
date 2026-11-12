package ec.edu.logistpulse.dominio;

/** Capacidad de la zona de preparación para reservas, no del almacenamiento físico. */
public class Almacen {
    private final int capacidadPreparacion;
    private int ocupacionPreparacion;

    public Almacen(int capacidadPreparacion) {
        if (capacidadPreparacion < 0) {
            throw new IllegalArgumentException("La capacidad no puede ser negativa");
        }
        this.capacidadPreparacion = capacidadPreparacion;
    }

    public int getCapacidadPreparacion() { return capacidadPreparacion; }
    public int getOcupacionPreparacion() { return ocupacionPreparacion; }
    public int getEspacioLibre() { return capacidadPreparacion - ocupacionPreparacion; }

    public void ocuparPreparacion(int cantidad) {
        if (cantidad <= 0 || cantidad > getEspacioLibre()) {
            throw new IllegalStateException("Capacidad de preparación insuficiente");
        }
        ocupacionPreparacion += cantidad;
    }

    public void liberarPreparacion(int cantidad) {
        if (cantidad <= 0 || cantidad > ocupacionPreparacion) {
            throw new IllegalStateException("Cantidad de liberación inválida");
        }
        ocupacionPreparacion -= cantidad;
    }
}
