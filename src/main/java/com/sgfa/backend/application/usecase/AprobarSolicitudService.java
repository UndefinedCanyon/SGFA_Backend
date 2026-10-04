package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.AprobarSolicitudUseCase;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.Inscripcion;

public class AprobarSolicitudService implements AprobarSolicitudUseCase {

    private final InscripcionRepositoryPort inscripcionRepositoryPort;

    public AprobarSolicitudService(InscripcionRepositoryPort inscripcionRepositoryPort) {
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
    }

    @Override
    public Inscripcion aprobar(Long idInscripcion) {
        Inscripcion inscripcion = inscripcionRepositoryPort.buscarPorId(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no existe."));

        inscripcion.setEstado("APROBADA");
        return inscripcionRepositoryPort.guardar(inscripcion);
    }
}