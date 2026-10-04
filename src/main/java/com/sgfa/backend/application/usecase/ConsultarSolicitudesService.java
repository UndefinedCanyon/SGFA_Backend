package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ConsultarSolicitudesUseCase;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.Inscripcion;

import java.util.List;

public class ConsultarSolicitudesService implements ConsultarSolicitudesUseCase {

    private final InscripcionRepositoryPort inscripcionRepositoryPort;

    public ConsultarSolicitudesService(InscripcionRepositoryPort inscripcionRepositoryPort) {
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
    }

    @Override
    public List<Inscripcion> consultarPorArtesano(Long idArtesano) {
        return inscripcionRepositoryPort.listarPorArtesano(idArtesano);
    }

    @Override
    public List<Inscripcion> consultarTodas() {
        return inscripcionRepositoryPort.listarTodas();
    }
}