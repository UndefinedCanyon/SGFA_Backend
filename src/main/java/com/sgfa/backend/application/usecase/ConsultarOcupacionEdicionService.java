package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ConsultarOcupacionEdicionUseCase;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.OcupacionEdicion;

public class ConsultarOcupacionEdicionService implements ConsultarOcupacionEdicionUseCase {

    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;
    private final InscripcionRepositoryPort inscripcionRepositoryPort;

    public ConsultarOcupacionEdicionService(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort,
                                             InscripcionRepositoryPort inscripcionRepositoryPort) {
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
    }

    @Override
    public OcupacionEdicion consultar(Long idEdicion) {
        EdicionFeria edicion = edicionFeriaRepositoryPort.buscarPorId(idEdicion)
                .orElseThrow(() -> new IllegalArgumentException("La edición no existe."));

        long aprobados = inscripcionRepositoryPort.contarAprobadasPorEdicion(idEdicion);
        return new OcupacionEdicion(edicion.getCupoMaximo(), aprobados);
    }
}