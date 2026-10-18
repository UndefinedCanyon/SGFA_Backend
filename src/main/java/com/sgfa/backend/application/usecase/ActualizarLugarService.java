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
    public Lugar actualizar(Long idLugar, String nombre, String direccion, Double latitud, Double longitud) {
        ValidacionLugar.validarTextos(nombre, direccion);
        ValidacionLugar.validarUbicacion(latitud, longitud);

        Lugar lugar = lugarRepositoryPort.buscarPorId(idLugar)
                .orElseThrow(() -> new IllegalArgumentException("El lugar no existe."));

        lugar.setNombre(nombre.trim());
        lugar.setDireccion(direccion.trim());
        lugar.setUbicacion(ValidacionLugar.redondear(latitud), ValidacionLugar.redondear(longitud));

        return lugarRepositoryPort.guardar(lugar);
    }
}