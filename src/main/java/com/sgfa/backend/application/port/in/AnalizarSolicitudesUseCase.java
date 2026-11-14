package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.AnalisisSolicitudes;

public interface AnalizarSolicitudesUseCase {

    /** Analiza las solicitudes pendientes de una edición y devuelve sugerencias para el administrador. */
    AnalisisSolicitudes analizar(Long idEdicionFeria);
}
