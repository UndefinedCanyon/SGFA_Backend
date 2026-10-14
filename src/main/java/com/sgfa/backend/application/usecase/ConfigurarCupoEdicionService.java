package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ConfigurarCupoEdicionUseCase;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;

public class ConfigurarCupoEdicionService implements ConfigurarCupoEdicionUseCase {

    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;
    private final InscripcionRepositoryPort inscripcionRepositoryPort;

    public ConfigurarCupoEdicionService(EdicionFeriaRepositoryPort edicionFeriaRepositoryPort,
                                         InscripcionRepositoryPort inscripcionRepositoryPort) {
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
    }

    @Override
    public EdicionFeria configurar(Long idEdicion, Integer cupoMaximo) {
        EdicionFeria edicion = edicionFeriaRepositoryPort.buscarPorId(idEdicion)
                .orElseThrow(() -> new IllegalArgumentException("La edición no existe."));

        if (cupoMaximo != null) {
            if (cupoMaximo < 1) {
                throw new DatoInvalidoException("El cupo debe ser de al menos 1 artesano.");
            }

            long aprobadas = inscripcionRepositoryPort.contarAprobadasPorEdicion(idEdicion);
            if (cupoMaximo < aprobadas) {
                throw new DatoInvalidoException(
                        "El cupo no puede ser menor a los " + aprobadas + " artesanos ya aprobados.");
            }
        }

        edicion.setCupoMaximo(cupoMaximo);
        return edicionFeriaRepositoryPort.guardar(edicion);
    }
}