package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.CrearEdicionFeriaUseCase;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Lugar;

import java.time.LocalDate;

public class CrearEdicionFeriaService implements CrearEdicionFeriaUseCase {

    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;
    private final LugarRepositoryPort lugarRepositoryPort;

    public CrearEdicionFeriaService(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort,
                                     LugarRepositoryPort lugarRepositoryPort) {
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
        this.lugarRepositoryPort = lugarRepositoryPort;
    }

    @Override
    public EdicionFeria crear(Long idFeria, Long idLugar, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new DatoInvalidoException("Indica la fecha de inicio y la de fin.");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new DatoInvalidoException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        Lugar lugar = lugarRepositoryPort.buscarPorId(idLugar)
                .orElseThrow(() -> new IllegalArgumentException("El lugar indicado no existe."));

        if (!lugar.isActivo()) {
            throw new DatoInvalidoException(
                    "El lugar \"" + lugar.getNombre() + "\" está desactivado. Elige otro o reactívalo.");
        }

        EdicionFeria nuevaEdicion = new EdicionFeria(null, idFeria, idLugar, fechaInicio, fechaFin, true, null);
        return edicionFeriaRepositoryPort.guardar(nuevaEdicion);
    }
}