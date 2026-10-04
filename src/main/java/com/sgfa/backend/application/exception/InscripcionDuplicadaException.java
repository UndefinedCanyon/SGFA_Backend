package com.sgfa.backend.application.exception;

public class InscripcionDuplicadaException extends RuntimeException {

    public InscripcionDuplicadaException(String mensaje) {
        super(mensaje);
    }
}