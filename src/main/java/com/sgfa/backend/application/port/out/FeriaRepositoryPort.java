package com.sgfa.backend.application.port.out;

import com.sgfa.backend.domain.model.Feria;

import java.util.List;
import java.util.Optional;

public interface FeriaRepositoryPort {

    Feria guardar(Feria feria);

    Optional<Feria> buscarPorId(Long id);

    List<Feria> listarTodas();
}