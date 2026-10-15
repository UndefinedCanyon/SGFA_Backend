package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Feria;

import java.util.List;

public interface ConsultarFeriasUseCase {

    List<Feria> consultarTodas();
}