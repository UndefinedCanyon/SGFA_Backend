package com.sgfa.backend.application.exception;

public class DatoDuplicadoException extends RuntimeException {

    public DatoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}