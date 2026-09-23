package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.EdicionFeria;

import java.util.List;

public interface ConsultarEdicionesFeriaUseCase {

    List<EdicionFeria> consultarTodas();

    List<EdicionFeria> consultarPorFeria(Long idFeria);
}