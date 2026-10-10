package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ConsultarArtesanosUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;

import java.util.List;

public class ConsultarArtesanosService implements ConsultarArtesanosUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;

    public ConsultarArtesanosService(ArtesanoRepositoryPort artesanoRepositoryPort) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
    }

    @Override
    public List<Artesano> consultarTodos() {
        return artesanoRepositoryPort.listarTodos();
    }

    @Override
    public Artesano consultarPorId(Long id) {
        return artesanoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("El artesano no existe."));
    }
}