package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Lugar;

public interface CrearLugarUseCase {

    Lugar crear(String nombre, String direccion);
}