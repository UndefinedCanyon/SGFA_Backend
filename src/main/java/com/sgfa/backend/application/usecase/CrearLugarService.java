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
    public Lugar crear(String nombre, String direccion, Double latitud, Double longitud) {
        ValidacionLugar.validarTextos(nombre, direccion);
        ValidacionLugar.validarUbicacion(latitud, longitud);

        Lugar nuevoLugar = new Lugar(
                null,
                nombre.trim(),
                direccion.trim(),
                ValidacionLugar.redondear(latitud),
                ValidacionLugar.redondear(longitud)
        );
        return lugarRepositoryPort.guardar(nuevoLugar);
    }
}