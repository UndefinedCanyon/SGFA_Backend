package com.sgfa.backend.application.port.out;

import com.sgfa.backend.domain.model.ContextoEdicion;
import com.sgfa.backend.domain.model.ResultadoAsistente;
import com.sgfa.backend.domain.model.ResumenSolicitud;

import java.util.List;

/** Salida hacia un asistente de IA externo. La aplicación no sabe qué proveedor hay detrás. */
public interface AsistenteIAPort {

    /** Nombre corto del proveedor, para mostrarlo en pantalla (por ejemplo "Gemini"). */
    String proveedor();

    /**
     * Sugiere una prioridad y un motivo para cada solicitud.
     *
     * @throws com.sgfa.backend.application.exception.AsistenteNoDisponibleException si no pudo responder
     * @throws com.sgfa.backend.application.exception.LimiteUsoAsistenteException si se superó el límite de uso
     */
    ResultadoAsistente priorizarSolicitudes(ContextoEdicion contexto, List<ResumenSolicitud> solicitudes);
}
