package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.RechazarSolicitudUseCase;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.Inscripcion;

public class RechazarSolicitudService implements RechazarSolicitudUseCase {

    private final InscripcionRepositoryPort inscripcionRepositoryPort;
    private final NotificadorSolicitud notificadorSolicitud;

    public RechazarSolicitudService(InscripcionRepositoryPort inscripcionRepositoryPort,
                                     NotificadorSolicitud notificadorSolicitud) {
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
        this.notificadorSolicitud = notificadorSolicitud;
    }

    @Override
    public Inscripcion rechazar(Long idInscripcion) {
        Inscripcion inscripcion = inscripcionRepositoryPort.buscarPorId(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no existe."));

        // Ya rechazada: no se cambia nada ni se vuelve a avisar.
        if ("RECHAZADA".equals(inscripcion.getEstado())) {
            return inscripcion;
        }

        boolean eraAprobada = "APROBADA".equals(inscripcion.getEstado());

        inscripcion.setEstado("RECHAZADA");
        Inscripcion guardada = inscripcionRepositoryPort.guardar(inscripcion);

        notificadorSolicitud.avisarRechazada(guardada, eraAprobada);
        return guardada;
    }
}