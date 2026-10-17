package com.sgfa.backend.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    @Value("${JWT_SECRET}")
    private String claveSecreta;

    @Value("${JWT_EXPIRATION_MS:7200000}")
    private long duracionMs;

    private SecretKey obtenerClave() {
        return Keys.hmacShaKeyFor(claveSecreta.getBytes());
    }

    public String generarToken(String correoElectronico, String rol, Long id) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + duracionMs);

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

    public Long extraerId(String token) {
        return extraerClaims(token).get("id", Long.class);
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