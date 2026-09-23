package com.sgfa.backend.application.port.out;

import com.sgfa.backend.domain.model.Artesano;

import java.util.List;
import java.util.Optional;

public interface ArtesanoRepositoryPort {

    Artesano guardar(Artesano artesano);

    Optional<Artesano> buscarPorId(Long id);

    List<Artesano> listarTodos();

    boolean existePorCorreo(String correoElectronico);

    boolean existePorCc(String cc);
}