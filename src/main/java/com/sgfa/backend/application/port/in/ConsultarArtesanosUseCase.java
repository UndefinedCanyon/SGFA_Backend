package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Artesano;

import java.util.List;

public interface ConsultarArtesanosUseCase {

    List<Artesano> consultarTodos();
}