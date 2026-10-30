package com.sgfa.backend.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoImagenTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

    @Test
    @DisplayName("Detecta cada formato por su firma, no por el nombre del archivo")
    void detecta_por_firma() {
        assertEquals(Optional.of(TipoImagen.JPEG), TipoImagen.detectar(JPEG));
        assertEquals(Optional.of(TipoImagen.PNG), TipoImagen.detectar(PNG));
        assertEquals(Optional.of(TipoImagen.WEBP), TipoImagen.detectar(WEBP));
    }

    @Test
    @DisplayName("Un contenido nulo, corto o desconocido no se reconoce")
    void no_reconoce() {
        assertTrue(TipoImagen.detectar(null).isEmpty());
        assertTrue(TipoImagen.detectar(new byte[0]).isEmpty());
        assertTrue(TipoImagen.detectar(new byte[]{(byte) 0xFF, (byte) 0xD8}).isEmpty());
        assertTrue(TipoImagen.detectar("<html></html>".getBytes()).isEmpty());
    }

    @Test
    @DisplayName("Un RIFF que no es WebP (por ejemplo un audio WAV) no se acepta")
    void riff_que_no_es_webp() {
        byte[] wav = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'A', 'V', 'E'};

        assertTrue(TipoImagen.detectar(wav).isEmpty());
    }

    @Test
    @DisplayName("Cada tipo conoce su extensión y su tipo MIME")
    void extension_y_mime() {
        assertEquals("jpg", TipoImagen.JPEG.getExtension());
        assertEquals("image/jpeg", TipoImagen.JPEG.getTipoMime());
        assertEquals("png", TipoImagen.PNG.getExtension());
        assertEquals("image/png", TipoImagen.PNG.getTipoMime());
        assertEquals("webp", TipoImagen.WEBP.getExtension());
        assertEquals("image/webp", TipoImagen.WEBP.getTipoMime());
    }

    @Test
    @DisplayName("Busca el tipo por extensión sin importar mayúsculas, y rechaza las desconocidas")
    void por_extension() {
        assertEquals(Optional.of(TipoImagen.JPEG), TipoImagen.dePorExtension("JPG"));
        assertEquals(Optional.of(TipoImagen.PNG), TipoImagen.dePorExtension("png"));
        assertEquals(Optional.of(TipoImagen.WEBP), TipoImagen.dePorExtension("WebP"));
        assertTrue(TipoImagen.dePorExtension("gif").isEmpty());
        assertTrue(TipoImagen.dePorExtension("jpeg").isEmpty());
    }
}