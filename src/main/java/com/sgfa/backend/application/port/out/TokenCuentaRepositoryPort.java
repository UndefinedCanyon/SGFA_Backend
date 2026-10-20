package com.sgfa.backend.application.port.out;

import com.sgfa.backend.domain.model.TipoTokenCuenta;
import com.sgfa.backend.domain.model.TokenCuenta;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenCuentaRepositoryPort {

    TokenCuenta guardar(TokenCuenta token);

    Optional<TokenCuenta> buscarPorHash(String tokenHash);

    Optional<LocalDateTime> buscarUltimaCreacion(Long idArtesano, TipoTokenCuenta tipo);

    /** Deja sin efecto los enlaces anteriores que aún no se han usado. */
    void invalidarPendientes(Long idArtesano, TipoTokenCuenta tipo);

    /** Marca el enlace como usado. Devuelve false si ya estaba usado (evita el doble uso). */
    boolean consumir(Long idToken);
}