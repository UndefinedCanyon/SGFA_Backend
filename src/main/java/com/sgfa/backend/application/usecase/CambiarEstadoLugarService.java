package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.CambiarEstadoLugarUseCase;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.domain.model.Lugar;

public class CambiarEstadoLugarService implements CambiarEstadoLugarUseCase {

    private final LugarRepositoryPort lugarRepositoryPort;

    public CambiarEstadoLugarService(LugarRepositoryPort lugarRepositoryPort) {
        this.lugarRepositoryPort = lugarRepositoryPort;
    }

    @Override
    public void cambiarEstado(Long idLugar, boolean activo) {
        Lugar lugar = lugarRepositoryPort.buscarPorId(idLugar)
                .orElseThrow(() -> new IllegalArgumentException("El lugar no existe."));

        lugar.setActivo(activo);
        lugarRepositoryPort.guardar(lugar);
    }
}