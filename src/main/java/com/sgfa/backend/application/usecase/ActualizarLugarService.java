package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ActualizarLugarUseCase;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.domain.model.Lugar;

public class ActualizarLugarService implements ActualizarLugarUseCase {

    private final LugarRepositoryPort lugarRepositoryPort;

    public ActualizarLugarService(LugarRepositoryPort lugarRepositoryPort) {
        this.lugarRepositoryPort = lugarRepositoryPort;
    }

    @Override
    public Lugar actualizar(Long id, String nombre, String direccion, Double latitud, Double longitud) {
        Lugar lugar = lugarRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("El lugar no existe."));

        ValidadorLugar.validar(nombre, direccion, latitud, longitud);

        lugar.setNombre(nombre.trim());
        lugar.setDireccion(direccion.trim());
        lugar.setLatitud(latitud);
        lugar.setLongitud(longitud);

        return lugarRepositoryPort.guardar(lugar);
    }
}