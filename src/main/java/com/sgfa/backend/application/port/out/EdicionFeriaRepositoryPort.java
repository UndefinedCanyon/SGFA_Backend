package com.sgfa.backend.application.port.out;

import com.sgfa.backend.domain.model.EdicionFeria;

import java.util.List;
import java.util.Optional;

public interface EdicionFeriaRepositoryPort {

    EdicionFeria guardar(EdicionFeria edicionFeria);

    Optional<EdicionFeria> buscarPorId(Long id);

    List<EdicionFeria> listarTodas();

    List<EdicionFeria> listarPorFeria(Long idFeria);
}