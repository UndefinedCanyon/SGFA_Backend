package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ConsultarFeriasUseCase;
import com.sgfa.backend.application.port.out.FeriaRepositoryPort;
import com.sgfa.backend.domain.model.Feria;

import java.util.List;

public class ConsultarFeriasService implements ConsultarFeriasUseCase {

    private final FeriaRepositoryPort feriaRepositoryPort;

    public ConsultarFeriasService(FeriaRepositoryPort feriaRepositoryPort) {
        this.feriaRepositoryPort = feriaRepositoryPort;
    }

    @Override
    public List<Feria> consultarTodas() {
        return feriaRepositoryPort.listarTodas();
    }
}