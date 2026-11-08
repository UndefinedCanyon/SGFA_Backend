package com.sgfa.backend.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SesionActualTest {

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("devuelve al usuario autenticado de la petición")
    void devuelve_el_usuario_autenticado() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(7L, "maria@correo.com", "ARTESANO");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));

        assertSame(usuario, SesionActual.usuario());
    }

    @Test
    @DisplayName("falla con un mensaje claro si no hay sesión")
    void falla_si_no_hay_sesion() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationCredentialsNotFoundException.class, SesionActual::usuario);
    }

    @Test
    @DisplayName("falla si quien hace la petición no es un UsuarioAutenticado")
    void falla_si_el_principal_es_de_otro_tipo() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("anonimo", null, List.of()));

        assertThrows(AuthenticationCredentialsNotFoundException.class, SesionActual::usuario);
    }
}