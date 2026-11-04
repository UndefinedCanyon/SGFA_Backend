package com.sgfa.backend.infrastructure.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Punto único para obtener al usuario que hizo la petición. Si no hay una sesión autenticada
 * lanza una excepción clara en vez de un NullPointerException.
 */
public final class SesionActual {

    private SesionActual() {
    }

    public static UsuarioAutenticado usuario() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        throw new AuthenticationCredentialsNotFoundException("No hay una sesión autenticada.");
    }
}