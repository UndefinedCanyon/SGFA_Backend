package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.EdicionFeria;

import java.time.LocalDate;

public interface CrearEdicionFeriaUseCase {

    EdicionFeria crear(Long idFeria, Long idLugar, LocalDate fechaInicio, LocalDate fechaFin);
}