package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.CambiarEstadoEdicionFeriaUseCase;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;

public class CambiarEstadoEdicionFeriaService implements CambiarEstadoEdicionFeriaUseCase {

    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;

    public CambiarEstadoEdicionFeriaService(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
    }

    @Override
    public void cambiarEstado(Long idEdicionFeria, boolean activo) {
        edicionFeriaRepositoryPort.buscarPorId(idEdicionFeria)
                .orElseThrow(() -> new IllegalArgumentException("La edición no existe."));

        edicionFeriaRepositoryPort.cambiarEstadoActivo(idEdicionFeria, activo);
    }
}