package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.CorreoNoVerificadoException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.exception.TokenInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ManejadorGlobalErroresTest {

    private final ManejadorGlobalErrores manejador = new ManejadorGlobalErrores();

    @Test
    @DisplayName("correo sin verificar responde 403 con su código")
    void correo_no_verificado_responde_403() {
        ResponseEntity<Map<String, String>> respuesta =
                manejador.manejarCorreoNoVerificado(new CorreoNoVerificadoException("Confirma tu correo"));

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertEquals(Map.of("error", "Confirma tu correo", "codigo", "CORREO_NO_VERIFICADO"), respuesta.getBody());
    }

    @Test
    @DisplayName("token inválido responde 400 con su código")
    void token_invalido_responde_400() {
        ResponseEntity<Map<String, String>> respuesta =
                manejador.manejarTokenInvalido(new TokenInvalidoException("El enlace venció"));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(Map.of("error", "El enlace venció", "codigo", "TOKEN_INVALIDO"), respuesta.getBody());
    }

    @Test
    @DisplayName("dato inválido responde 400 solo con el mensaje")
    void dato_invalido_responde_400() {
        ResponseEntity<Map<String, String>> respuesta =
                manejador.manejarDatoInvalido(new DatoInvalidoException("El nombre es obligatorio"));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(Map.of("error", "El nombre es obligatorio"), respuesta.getBody());
    }

    @Test
    @DisplayName("un error inesperado responde 500 sin revelar el detalle interno")
    void error_inesperado_responde_500_sin_filtrar_detalles() {
        ResponseEntity<Map<String, String>> respuesta =
                manejador.manejarErrorInesperado(new IllegalStateException("clave-secreta-de-la-base-de-datos"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertFalse(respuesta.getBody().get("error").contains("clave-secreta"));
        assertEquals(1, respuesta.getBody().size());
    }
}