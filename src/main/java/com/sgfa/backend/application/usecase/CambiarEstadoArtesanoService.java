package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.CambiarEstadoArtesanoUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;

public class CambiarEstadoArtesanoService implements CambiarEstadoArtesanoUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;

    public CambiarEstadoArtesanoService(ArtesanoRepositoryPort artesanoRepositoryPort) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
    }

    @Override
    public void cambiarEstado(Long idArtesano, boolean activo) {
        artesanoRepositoryPort.buscarPorId(idArtesano)
                .orElseThrow(() -> new IllegalArgumentException("El artesano no existe."));

        artesanoRepositoryPort.cambiarEstadoActivo(idArtesano, activo);
    }
}