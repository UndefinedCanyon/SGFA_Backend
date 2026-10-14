package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.EdicionFeria;

public interface ConfigurarCupoEdicionUseCase {

    EdicionFeria configurar(Long idEdicion, Integer cupoMaximo);
}