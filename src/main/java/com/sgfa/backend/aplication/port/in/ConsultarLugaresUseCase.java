package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Lugar;

import java.util.List;

public interface ConsultarLugaresUseCase {

    List<Lugar> consultarTodos();
}