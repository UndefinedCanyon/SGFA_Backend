package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.RechazarSolicitudUseCase;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.Inscripcion;

public class RechazarSolicitudService implements RechazarSolicitudUseCase {

    private final InscripcionRepositoryPort inscripcionRepositoryPort;

    public RechazarSolicitudService(InscripcionRepositoryPort inscripcionRepositoryPort) {
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
    }

    @Override
    public Inscripcion rechazar(Long idInscripcion) {
        Inscripcion inscripcion = inscripcionRepositoryPort.buscarPorId(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no existe."));

        inscripcion.setEstado("RECHAZADA");
        return inscripcionRepositoryPort.guardar(inscripcion);
    }
}