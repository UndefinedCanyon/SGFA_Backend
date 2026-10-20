package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

final class DatosCuenta {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int LARGO_MINIMO_CONTRASENA = 8;
    private static final int BYTES_MAXIMOS_CONTRASENA = 72;

    private DatosCuenta() {
    }

    static String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    static void validarCorreo(String correoNormalizado) {
        if (correoNormalizado.isEmpty()
                || correoNormalizado.length() > 150
                || !CORREO.matcher(correoNormalizado).matches()) {
            throw new DatoInvalidoException("Indica un correo electrónico válido.");
        }
    }

    static void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < LARGO_MINIMO_CONTRASENA) {
            throw new DatoInvalidoException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS_CONTRASENA) {
            throw new DatoInvalidoException("La contraseña es demasiado larga (máximo 72 bytes).");
        }
    }

    static void validarTexto(String etiqueta, String valor, int maximo, boolean obligatorio) {
        if (valor == null || valor.isBlank()) {
            if (obligatorio) {
                throw new DatoInvalidoException(etiqueta + " es obligatorio.");
            }
            return;
        }
        if (valor.trim().length() > maximo) {
            throw new DatoInvalidoException(etiqueta + " no puede superar los " + maximo + " caracteres.");
        }
    }
}