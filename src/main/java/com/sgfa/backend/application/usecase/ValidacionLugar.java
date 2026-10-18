package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;

final class ValidacionLugar {

    private ValidacionLugar() {
    }

    static void validarTextos(String nombre, String direccion) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre del lugar es obligatorio.");
        }
        if (direccion == null || direccion.isBlank()) {
            throw new DatoInvalidoException("La dirección del lugar es obligatoria.");
        }
    }

    static void validarUbicacion(Double latitud, Double longitud) {
        if (latitud == null && longitud == null) {
            return;
        }
        if (latitud == null || longitud == null) {
            throw new DatoInvalidoException("Indica la latitud y la longitud, o ninguna de las dos.");
        }
        if (!esFinito(latitud) || latitud < -90 || latitud > 90) {
            throw new DatoInvalidoException("La latitud debe estar entre -90 y 90.");
        }
        if (!esFinito(longitud) || longitud < -180 || longitud > 180) {
            throw new DatoInvalidoException("La longitud debe estar entre -180 y 180.");
        }
    }

    // Seis decimales equivalen a unos 11 cm, que es lo que guarda la columna DECIMAL(9,6).
    static Double redondear(Double valor) {
        return valor == null ? null : Math.round(valor * 1_000_000d) / 1_000_000d;
    }

    private static boolean esFinito(Double valor) {
        return !valor.isNaN() && !valor.isInfinite();
    }
}