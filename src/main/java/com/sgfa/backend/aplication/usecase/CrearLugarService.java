package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.CrearLugarUseCase;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.domain.model.Lugar;

public class CrearLugarService implements CrearLugarUseCase {

    private final LugarRepositoryPort lugarRepositoryPort;

    public CrearLugarService(LugarRepositoryPort lugarRepositoryPort) {
        this.lugarRepositoryPort = lugarRepositoryPort;
    }

    @Override
    public Lugar crear(String nombre, String direccion) {
        Lugar nuevoLugar = new Lugar(null, nombre, direccion);
        return lugarRepositoryPort.guardar(nuevoLugar);
    }
}