package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;

import java.math.BigDecimal;

final class ValidadorProducto {

    static final int LARGO_MAXIMO_NOMBRE = 150;
    static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");
    static final int CANTIDAD_MAXIMA = 1_000_000;

    private ValidadorProducto() {
    }

    static void validar(String nombre, BigDecimal precio, Integer cantidad) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre del producto es obligatorio.");
        }
        if (nombre.trim().length() > LARGO_MAXIMO_NOMBRE) {
            throw new DatoInvalidoException(
                    "El nombre del producto no puede superar los " + LARGO_MAXIMO_NOMBRE + " caracteres.");
        }

        if (precio == null) {
            throw new DatoInvalidoException("Indica el precio del producto.");
        }
        if (precio.signum() <= 0) {
            throw new DatoInvalidoException("El precio debe ser mayor a 0.");
        }
        if (precio.stripTrailingZeros().scale() > 2) {
            throw new DatoInvalidoException("El precio admite como máximo 2 decimales.");
        }
        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new DatoInvalidoException("El precio es demasiado alto.");
        }

        if (cantidad == null) {
            throw new DatoInvalidoException("Indica la cantidad disponible.");
        }
        if (cantidad < 0) {
            throw new DatoInvalidoException("La cantidad no puede ser negativa.");
        }
        if (cantidad > CANTIDAD_MAXIMA) {
            throw new DatoInvalidoException(
                    "La cantidad no puede superar las " + CANTIDAD_MAXIMA + " unidades.");
        }
    }
}