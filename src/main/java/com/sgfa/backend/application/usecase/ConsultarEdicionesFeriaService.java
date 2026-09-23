package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ConsultarEdicionesFeriaUseCase;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;

import java.util.List;

public class ConsultarEdicionesFeriaService implements ConsultarEdicionesFeriaUseCase {

    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;

    public ConsultarEdicionesFeriaService(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
    }

    @Override
    public List<EdicionFeria> consultarTodas() {
        return edicionFeriaRepositoryPort.listarTodas();
    }

    @Override
    public List<EdicionFeria> consultarPorFeria(Long idFeria) {
        return edicionFeriaRepositoryPort.listarPorFeria(idFeria);
    }
}