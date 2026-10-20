package com.sgfa.backend.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum TipoImagen {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp");

    private final String extension;
    private final String tipoMime;

    TipoImagen(String extension, String tipoMime) {
        this.extension = extension;
        this.tipoMime = tipoMime;
    }

    public String getExtension() {
        return extension;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public static Optional<TipoImagen> dePorExtension(String extension) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.extension.equalsIgnoreCase(extension))
                .findFirst();
    }

    public static Optional<TipoImagen> detectar(byte[] contenido) {
        if (contenido == null) {
            return Optional.empty();
        }
        if (esJpeg(contenido)) {
            return Optional.of(JPEG);
        }
        if (esPng(contenido)) {
            return Optional.of(PNG);
        }
        if (esWebp(contenido)) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean esJpeg(byte[] c) {
        return c.length >= 3
                && (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF;
    }

    private static boolean esPng(byte[] c) {
        return c.length >= 8
                && (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G'
                && c[4] == 0x0D && c[5] == 0x0A && c[6] == 0x1A && c[7] == 0x0A;
    }

    private static boolean esWebp(byte[] c) {
        return c.length >= 12
                && c[0] == 'R' && c[1] == 'I' && c[2] == 'F' && c[3] == 'F'
                && c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P';
    }
}