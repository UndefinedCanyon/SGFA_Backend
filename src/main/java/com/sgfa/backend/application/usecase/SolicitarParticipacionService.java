package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.InscripcionDuplicadaException;
import com.sgfa.backend.application.port.in.SolicitarParticipacionUseCase;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.Inscripcion;

import java.time.LocalDate;

public class SolicitarParticipacionService implements SolicitarParticipacionUseCase {

    private final InscripcionRepositoryPort inscripcionRepositoryPort;

    public SolicitarParticipacionService(InscripcionRepositoryPort inscripcionRepositoryPort) {
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
    }

    @Override
    public Inscripcion solicitar(Long idArtesano, Long idEdicionFeria) {
        if (inscripcionRepositoryPort.existeInscripcion(idArtesano, idEdicionFeria)) {
            throw new InscripcionDuplicadaException("Ya has solicitado participar en esta edición.");
        }

        Inscripcion nuevaInscripcion = new Inscripcion(null, idArtesano, idEdicionFeria, LocalDate.now(), "PENDIENTE");
        return inscripcionRepositoryPort.guardar(nuevaInscripcion);
    }
}