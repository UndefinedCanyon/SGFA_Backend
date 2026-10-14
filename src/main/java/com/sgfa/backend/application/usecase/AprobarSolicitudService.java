package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.CupoAgotadoException;
import com.sgfa.backend.application.port.in.AprobarSolicitudUseCase;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Inscripcion;

public class AprobarSolicitudService implements AprobarSolicitudUseCase {

    private final InscripcionRepositoryPort inscripcionRepositoryPort;
    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;

    public AprobarSolicitudService(InscripcionRepositoryPort inscripcionRepositoryPort,
                                    EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
    }

    @Override
    public Inscripcion aprobar(Long idInscripcion) {
        Inscripcion inscripcion = inscripcionRepositoryPort.buscarPorId(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no existe."));

        if ("APROBADA".equals(inscripcion.getEstado())) {
            return inscripcion;
        }

        EdicionFeria edicion = edicionFeriaRepositoryPort.buscarPorId(inscripcion.getIdEdicionFeria())
                .orElseThrow(() -> new IllegalArgumentException("La edición no existe."));

        if (edicion.getCupoMaximo() != null) {
            long aprobadas = inscripcionRepositoryPort.contarAprobadasPorEdicion(edicion.getId());
            if (aprobadas >= edicion.getCupoMaximo()) {
                throw new CupoAgotadoException(
                        "La edición ya alcanzó su cupo máximo de " + edicion.getCupoMaximo() + " artesanos.");
            }
        }

        inscripcion.setEstado("APROBADA");
        return inscripcionRepositoryPort.guardar(inscripcion);
    }
}