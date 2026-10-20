package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.TokenInvalidoException;
import com.sgfa.backend.application.port.in.VerificarCorreoUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.TokenCuentaRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.TipoTokenCuenta;
import com.sgfa.backend.domain.model.TokenCuenta;

import java.time.LocalDateTime;

public class VerificarCorreoService implements VerificarCorreoUseCase {

    private static final String MENSAJE_INVALIDO =
            "El enlace no es válido, ya se usó o venció. Si ya confirmaste tu correo, inicia sesión; si no, solicita un enlace nuevo.";

    private final TokenCuentaRepositoryPort tokenCuentaRepositoryPort;
    private final ArtesanoRepositoryPort artesanoRepositoryPort;

    public VerificarCorreoService(TokenCuentaRepositoryPort tokenCuentaRepositoryPort,
                                   ArtesanoRepositoryPort artesanoRepositoryPort) {
        this.tokenCuentaRepositoryPort = tokenCuentaRepositoryPort;
        this.artesanoRepositoryPort = artesanoRepositoryPort;
    }

    @Override
    public void verificar(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenInvalidoException(MENSAJE_INVALIDO);
        }

        TokenCuenta tokenCuenta = tokenCuentaRepositoryPort.buscarPorHash(TokenSeguro.hash(token.trim()))
                .filter(t -> t.getTipo() == TipoTokenCuenta.VERIFICACION)
                .filter(t -> t.estaVigente(LocalDateTime.now()))
                .orElseThrow(() -> new TokenInvalidoException(MENSAJE_INVALIDO));

        if (!tokenCuentaRepositoryPort.consumir(tokenCuenta.getId())) {
            throw new TokenInvalidoException(MENSAJE_INVALIDO);
        }

        Artesano artesano = artesanoRepositoryPort.buscarPorId(tokenCuenta.getIdArtesano())
                .orElseThrow(() -> new TokenInvalidoException(MENSAJE_INVALIDO));

        artesano.setCorreoVerificado(true);
        artesanoRepositoryPort.guardar(artesano);
    }
}