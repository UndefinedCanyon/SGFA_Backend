package com.sgfa.backend.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    // Clave secreta usada para firmar los tokens.
    // En un entorno real de producción, esto debería venir de una variable de entorno, no estar escrito en el código.
    private static final String CLAVE_SECRETA = "sgfa-clave-secreta-para-firmar-tokens-jwt-2026-super-larga";

    private static final long DURACION_MS = 1000 * 60 * 60 * 2; // 2 horas

    private SecretKey obtenerClave() {
        return Keys.hmacShaKeyFor(CLAVE_SECRETA.getBytes());
    }

    public String generarToken(String correoElectronico, String rol, Long id) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + DURACION_MS);

        return Jwts.builder()
                .subject(correoElectronico)
                .claim("rol", rol)
                .claim("id", id)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(obtenerClave())
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerCorreo(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    public boolean esTokenValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}