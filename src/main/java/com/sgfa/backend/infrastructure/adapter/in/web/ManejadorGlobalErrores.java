package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.CorreoNoVerificadoException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.exception.TokenInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
public class ManejadorGlobalErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(CorreoNoVerificadoException.class)
    public ResponseEntity<Map<String, String>> manejarCorreoNoVerificado(CorreoNoVerificadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage(), "codigo", "CORREO_NO_VERIFICADO"));
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<Map<String, String>> manejarTokenInvalido(TokenInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage(), "codigo", "TOKEN_INVALIDO"));
    }

    @ExceptionHandler(DatoInvalidoException.class)
    public ResponseEntity<Map<String, String>> manejarDatoInvalido(DatoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorInesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ocurrió un error inesperado. Intenta de nuevo más tarde."));
    }
}