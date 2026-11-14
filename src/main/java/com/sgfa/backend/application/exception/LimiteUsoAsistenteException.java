package com.sgfa.backend.application.exception;

/** Se pidieron demasiados análisis en poco tiempo; protege la cuota gratuita del proveedor. */
public class LimiteUsoAsistenteException extends RuntimeException {

    public LimiteUsoAsistenteException(String mensaje) {
        super(mensaje);
    }
}
