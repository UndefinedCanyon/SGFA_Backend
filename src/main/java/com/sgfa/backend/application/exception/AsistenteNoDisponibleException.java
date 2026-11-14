package com.sgfa.backend.application.exception;

/** El asistente de IA no pudo responder (sin conexión, cuota agotada, respuesta inválida...). */
public class AsistenteNoDisponibleException extends RuntimeException {

    public AsistenteNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public AsistenteNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
