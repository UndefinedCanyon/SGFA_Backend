package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.domain.model.TipoImagen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidacionImagenTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

    @Test
    @DisplayName("Reconoce JPG, PNG y WebP por su contenido")
    void formatos_permitidos() {
        assertEquals(TipoImagen.JPEG, ValidacionImagen.validar(JPEG));
        assertEquals(TipoImagen.PNG, ValidacionImagen.validar(PNG));
        assertEquals(TipoImagen.WEBP, ValidacionImagen.validar(WEBP));
    }

    @Test
    @DisplayName("Un archivo nulo o vacío se rechaza")
    void archivo_vacio() {
        assertEquals("El archivo está vacío.",
                assertThrows(DatoInvalidoException.class, () -> ValidacionImagen.validar(null)).getMessage());
        assertEquals("El archivo está vacío.",
                assertThrows(DatoInvalidoException.class, () -> ValidacionImagen.validar(new byte[0])).getMessage());
    }

    @Test
    @DisplayName("Una imagen de más de 2 MB se rechaza, y una de exactamente 2 MB se acepta")
    void limite_de_tamano() {
        byte[] justoEnElLimite = new byte[ValidacionImagen.TAMANO_MAXIMO_BYTES];
        System.arraycopy(PNG, 0, justoEnElLimite, 0, PNG.length);
        assertEquals(TipoImagen.PNG, ValidacionImagen.validar(justoEnElLimite));

        byte[] pasadaDelLimite = new byte[ValidacionImagen.TAMANO_MAXIMO_BYTES + 1];
        System.arraycopy(PNG, 0, pasadaDelLimite, 0, PNG.length);
        assertEquals("La imagen no puede superar los 2 MB.",
                assertThrows(DatoInvalidoException.class, () -> ValidacionImagen.validar(pasadaDelLimite)).getMessage());
    }

    @Test
    @DisplayName("Un formato no permitido (como GIF o texto) se rechaza")
    void formato_no_permitido() {
        byte[] gif = "GIF89a-contenido".getBytes();
        byte[] texto = "esto no es una imagen".getBytes();

        assertEquals("Formato no permitido. Usa una imagen JPG, PNG o WebP.",
                assertThrows(DatoInvalidoException.class, () -> ValidacionImagen.validar(gif)).getMessage());
        assertThrows(DatoInvalidoException.class, () -> ValidacionImagen.validar(texto));
    }
}