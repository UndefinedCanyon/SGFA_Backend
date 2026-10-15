package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.CrearEdicionFeriaUseCase;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;

import java.time.LocalDate;

public class CrearEdicionFeriaService implements CrearEdicionFeriaUseCase {

    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;

    public CrearEdicionFeriaService(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort) {
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
    }

    @Override
    public EdicionFeria crear(Long idFeria, Long idLugar, LocalDate fechaInicio, LocalDate fechaFin) {
        EdicionFeria nuevaEdicion = new EdicionFeria(null, idFeria, idLugar, fechaInicio, fechaFin);
        return edicionFeriaRepositoryPort.guardar(nuevaEdicion);
    }
}