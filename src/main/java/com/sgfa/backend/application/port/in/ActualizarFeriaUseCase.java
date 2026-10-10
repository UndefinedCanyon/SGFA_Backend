package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Feria;

public interface ActualizarFeriaUseCase {

    Feria actualizar(Long idFeria, String nombreFeria);
}