package com.sgfa.backend.application.exception;

public class CorreoNoVerificadoException extends RuntimeException {

    public CorreoNoVerificadoException(String mensaje) {
        super(mensaje);
    }
}