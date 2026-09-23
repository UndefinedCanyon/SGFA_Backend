package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ConsultarLugaresUseCase;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.domain.model.Lugar;

import java.util.List;

public class ConsultarLugaresService implements ConsultarLugaresUseCase {

    private final LugarRepositoryPort lugarRepositoryPort;

    public ConsultarLugaresService(LugarRepositoryPort lugarRepositoryPort) {
        this.lugarRepositoryPort = lugarRepositoryPort;
    }

    @Override
    public List<Lugar> consultarTodos() {
        return lugarRepositoryPort.listarTodos();
    }
}