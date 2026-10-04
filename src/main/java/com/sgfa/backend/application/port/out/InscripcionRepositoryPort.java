package com.sgfa.backend.application.port.out;

import com.sgfa.backend.domain.model.Inscripcion;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepositoryPort {

    Inscripcion guardar(Inscripcion inscripcion);

    Optional<Inscripcion> buscarPorId(Long id);

    List<Inscripcion> listarPorArtesano(Long idArtesano);

    List<Inscripcion> listarTodas();

    boolean existeInscripcion(Long idArtesano, Long idEdicionFeria);
}