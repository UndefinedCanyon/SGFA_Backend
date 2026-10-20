package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.domain.model.TipoImagen;

final class ValidacionImagen {

    static final int TAMANO_MAXIMO_BYTES = 2 * 1024 * 1024;

    private ValidacionImagen() {
    }

    static TipoImagen validar(byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new DatoInvalidoException("El archivo está vacío.");
        }
        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new DatoInvalidoException("La imagen no puede superar los 2 MB.");
        }
        return TipoImagen.detectar(contenido)
                .orElseThrow(() -> new DatoInvalidoException(
                        "Formato no permitido. Usa una imagen JPG, PNG o WebP."));
    }
}