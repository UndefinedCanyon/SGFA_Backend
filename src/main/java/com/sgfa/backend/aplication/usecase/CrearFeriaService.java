package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.CrearFeriaUseCase;
import com.sgfa.backend.application.port.out.FeriaRepositoryPort;
import com.sgfa.backend.domain.model.Feria;

public class CrearFeriaService implements CrearFeriaUseCase {

    private final FeriaRepositoryPort feriaRepositoryPort;

    public CrearFeriaService(FeriaRepositoryPort feriaRepositoryPort) {
        this.feriaRepositoryPort = feriaRepositoryPort;
    }

    @Override
    public Feria crear(String nombreFeria, Long idAdmin) {
        Feria nuevaFeria = new Feria(null, nombreFeria, idAdmin);
        return feriaRepositoryPort.guardar(nuevaFeria);
    }
}