package ec.edu.logistpulse.servicio;

/** Datos de una solicitud; los manejadores de la cadena realizan las validaciones. */
public record SolicitudReserva(String sku, String loteId, int cantidad, String solicitante) { }
