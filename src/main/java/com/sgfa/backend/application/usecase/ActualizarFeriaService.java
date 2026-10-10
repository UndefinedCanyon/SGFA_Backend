package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarFeriaUseCase;
import com.sgfa.backend.application.port.out.FeriaRepositoryPort;
import com.sgfa.backend.domain.model.Feria;

public class ActualizarFeriaService implements ActualizarFeriaUseCase {

    private final FeriaRepositoryPort feriaRepositoryPort;

    public ActualizarFeriaService(FeriaRepositoryPort feriaRepositoryPort) {
        this.feriaRepositoryPort = feriaRepositoryPort;
    }

    @Override
    public Feria actualizar(Long idFeria, String nombreFeria) {
        if (nombreFeria == null || nombreFeria.isBlank()) {
            throw new DatoInvalidoException("El nombre de la feria es obligatorio.");
        }

        Feria feria = feriaRepositoryPort.buscarPorId(idFeria)
                .orElseThrow(() -> new IllegalArgumentException("La feria no existe."));

        feria.setNombreFeria(nombreFeria.trim());
        return feriaRepositoryPort.guardar(feria);
    }
}