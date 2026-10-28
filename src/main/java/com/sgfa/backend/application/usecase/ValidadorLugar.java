package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;

final class ValidadorLugar {

    private ValidadorLugar() {
    }

    static void validar(String nombre, String direccion, Double latitud, Double longitud) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre del lugar es obligatorio.");
        }
        if (nombre.trim().length() > 150) {
            throw new DatoInvalidoException("El nombre del lugar no puede superar los 150 caracteres.");
        }
        if (direccion == null || direccion.isBlank()) {
            throw new DatoInvalidoException("La dirección del lugar es obligatoria.");
        }
        if (direccion.trim().length() > 255) {
            throw new DatoInvalidoException("La dirección del lugar no puede superar los 255 caracteres.");
        }
        if ((latitud == null) != (longitud == null)) {
            throw new DatoInvalidoException("La latitud y la longitud deben indicarse juntas.");
        }
        if (latitud != null) {
            if (!(latitud >= -90 && latitud <= 90)) {
                throw new DatoInvalidoException("La latitud debe estar entre -90 y 90.");
            }
            if (!(longitud >= -180 && longitud <= 180)) {
                throw new DatoInvalidoException("La longitud debe estar entre -180 y 180.");
            }
        }
    }
}