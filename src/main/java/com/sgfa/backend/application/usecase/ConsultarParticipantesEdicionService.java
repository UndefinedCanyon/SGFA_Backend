package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ConsultarParticipantesEdicionUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Inscripcion;
import com.sgfa.backend.domain.model.ParticipantePublico;

import java.util.ArrayList;
import java.util.List;

public class ConsultarParticipantesEdicionService implements ConsultarParticipantesEdicionUseCase {

    private final InscripcionRepositoryPort inscripcionRepositoryPort;
    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;

    public ConsultarParticipantesEdicionService(InscripcionRepositoryPort inscripcionRepositoryPort,
                                                 ArtesanoRepositoryPort artesanoRepositoryPort,
                                                 ProductoRepositoryPort productoRepositoryPort,
                                                 EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
    }

    @Override
    public List<ParticipantePublico> consultar(Long idEdicionFeria) {
        edicionFeriaRepositoryPort.buscarPorId(idEdicionFeria)
                .filter(EdicionFeria::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("La edición no existe."));

        List<ParticipantePublico> participantes = new ArrayList<>();

        for (Inscripcion inscripcion : inscripcionRepositoryPort.listarAprobadasPorEdicion(idEdicionFeria)) {
            artesanoRepositoryPort.buscarPorId(inscripcion.getIdArtesano())
                    .filter(Artesano::isActivo)
                    .ifPresent(artesano -> participantes.add(new ParticipantePublico(
                            artesano.getId(),
                            artesano.getNombreEmprendimiento(),
                            artesano.getDescripcionCorta(),
                            productoRepositoryPort.listarPorArtesano(artesano.getId())
                    )));
        }

        return participantes;
    }
}