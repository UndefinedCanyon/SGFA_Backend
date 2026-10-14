package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.OcupacionEdicion;

public interface ConsultarOcupacionEdicionUseCase {

    OcupacionEdicion consultar(Long idEdicion);
}