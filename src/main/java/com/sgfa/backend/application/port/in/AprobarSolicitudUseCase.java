package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Inscripcion;

public interface AprobarSolicitudUseCase {

    Inscripcion aprobar(Long idInscripcion);
}