package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Feria;

public interface CrearFeriaUseCase {

    Feria crear(String nombreFeria, Long idAdmin);
}