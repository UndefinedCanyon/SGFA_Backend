package com.sgfa.backend.domain.model;

import java.time.LocalDateTime;

public class TokenCuenta {

    private final Long id;
    private final Long idArtesano;
    private final TipoTokenCuenta tipo;
    private final String tokenHash;
    private final LocalDateTime creadoEn;
    private final LocalDateTime expiraEn;
    private final boolean usado;

    public TokenCuenta(Long id, Long idArtesano, TipoTokenCuenta tipo, String tokenHash,
                       LocalDateTime creadoEn, LocalDateTime expiraEn, boolean usado) {
        this.id = id;
        this.idArtesano = idArtesano;
        this.tipo = tipo;
        this.tokenHash = tokenHash;
        this.creadoEn = creadoEn;
        this.expiraEn = expiraEn;
        this.usado = usado;
    }

    public Long getId() {
        return id;
    }

    public Long getIdArtesano() {
        return idArtesano;
    }

    public TipoTokenCuenta getTipo() {
        return tipo;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public boolean isUsado() {
        return usado;
    }

    public boolean estaVigente(LocalDateTime ahora) {
        return !usado && ahora.isBefore(expiraEn);
    }
}