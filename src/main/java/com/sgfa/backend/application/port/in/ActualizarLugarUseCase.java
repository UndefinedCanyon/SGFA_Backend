package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Lugar;

public interface ActualizarLugarUseCase {

    Lugar actualizar(Long idLugar, String nombre, String direccion, Double latitud, Double longitud);
}