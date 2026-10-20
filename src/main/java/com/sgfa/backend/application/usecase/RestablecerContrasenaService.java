package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.TokenInvalidoException;
import com.sgfa.backend.application.port.in.RestablecerContrasenaUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.CifradoContrasenaPort;
import com.sgfa.backend.application.port.out.TokenCuentaRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.TipoTokenCuenta;
import com.sgfa.backend.domain.model.TokenCuenta;

import java.time.LocalDateTime;

public class RestablecerContrasenaService implements RestablecerContrasenaUseCase {

    private static final String MENSAJE_INVALIDO =
            "El enlace no es válido, ya se usó o venció. Solicita uno nuevo desde \"Olvidé mi contraseña\".";

    private final TokenCuentaRepositoryPort tokenCuentaRepositoryPort;
    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final CifradoContrasenaPort cifradoContrasenaPort;

    public RestablecerContrasenaService(TokenCuentaRepositoryPort tokenCuentaRepositoryPort,
                                         ArtesanoRepositoryPort artesanoRepositoryPort,
                                         CifradoContrasenaPort cifradoContrasenaPort) {
        this.tokenCuentaRepositoryPort = tokenCuentaRepositoryPort;
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.cifradoContrasenaPort = cifradoContrasenaPort;
    }

    @Override
    public void restablecer(String token, String nuevaContrasena) {
        // Primero se valida la contraseña: si es débil, el enlace no se gasta y se puede reintentar.
        DatosCuenta.validarContrasena(nuevaContrasena);

        if (token == null || token.isBlank()) {
            throw new TokenInvalidoException(MENSAJE_INVALIDO);
        }

        TokenCuenta tokenCuenta = tokenCuentaRepositoryPort.buscarPorHash(TokenSeguro.hash(token.trim()))
                .filter(t -> t.getTipo() == TipoTokenCuenta.RECUPERACION)
                .filter(t -> t.estaVigente(LocalDateTime.now()))
                .orElseThrow(() -> new TokenInvalidoException(MENSAJE_INVALIDO));

        if (!tokenCuentaRepositoryPort.consumir(tokenCuenta.getId())) {
            throw new TokenInvalidoException(MENSAJE_INVALIDO);
        }

        Artesano artesano = artesanoRepositoryPort.buscarPorId(tokenCuenta.getIdArtesano())
                .orElseThrow(() -> new TokenInvalidoException(MENSAJE_INVALIDO));

        artesano.setContrasena(cifradoContrasenaPort.cifrar(nuevaContrasena));
        // Quien abre este enlace demuestra que controla el correo.
        artesano.setCorreoVerificado(true);
        artesanoRepositoryPort.guardar(artesano);
    }
}