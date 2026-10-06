package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.CambiarEstadoFeriaUseCase;
import com.sgfa.backend.application.port.out.FeriaRepositoryPort;

public class CambiarEstadoFeriaService implements CambiarEstadoFeriaUseCase {

    private final FeriaRepositoryPort feriaRepositoryPort;

    public CambiarEstadoFeriaService(FeriaRepositoryPort feriaRepositoryPort) {
        this.feriaRepositoryPort = feriaRepositoryPort;
    }

    @Override
    public void cambiarEstado(Long idFeria, boolean activo) {
        feriaRepositoryPort.buscarPorId(idFeria)
                .orElseThrow(() -> new IllegalArgumentException("La feria no existe."));

        feriaRepositoryPort.cambiarEstadoActivo(idFeria, activo);
    }
}