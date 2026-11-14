package com.sgfa.backend.domain.model;

import java.util.List;

/** Respuesta cruda del asistente: un resumen y una sugerencia por solicitud. */
public record ResultadoAsistente(String resumen, List<SugerenciaSolicitud> sugerencias) {
}
