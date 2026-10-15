package com.sgfa.backend.application.port.out;

import com.sgfa.backend.domain.model.Lugar;

import java.util.List;
import java.util.Optional;

public interface LugarRepositoryPort {

    Lugar guardar(Lugar lugar);

    Optional<Lugar> buscarPorId(Long id);

    List<Lugar> listarTodos();
}