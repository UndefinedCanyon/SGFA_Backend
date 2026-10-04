package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Inscripcion;

public interface RechazarSolicitudUseCase {

    Inscripcion rechazar(Long idInscripcion);
}