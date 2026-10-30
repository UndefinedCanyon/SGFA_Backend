package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorLugarTest {

    private static final String NOMBRE = "Parque Principal";
    private static final String DIRECCION = "Calle 6 # 7-10";

    private DatoInvalidoException rechazar(String nombre, String direccion, Double latitud, Double longitud) {
        return assertThrows(DatoInvalidoException.class,
                () -> ValidadorLugar.validar(nombre, direccion, latitud, longitud));
    }

    @Test
    @DisplayName("Un lugar con coordenadas válidas se acepta")
    void lugar_con_coordenadas() {
        assertDoesNotThrow(() -> ValidadorLugar.validar(NOMBRE, DIRECCION, 4.3369, -74.3636));
    }

    @Test
    @DisplayName("Un lugar sin coordenadas también se acepta")
    void lugar_sin_coordenadas() {
        assertDoesNotThrow(() -> ValidadorLugar.validar(NOMBRE, DIRECCION, null, null));
    }

    @Test
    @DisplayName("El nombre es obligatorio")
    void nombre_obligatorio() {
        assertEquals("El nombre del lugar es obligatorio.", rechazar(null, DIRECCION, null, null).getMessage());
        assertEquals("El nombre del lugar es obligatorio.", rechazar("   ", DIRECCION, null, null).getMessage());
    }

    @Test
    @DisplayName("El nombre admite 150 caracteres y rechaza 151")
    void nombre_largo() {
        assertDoesNotThrow(() -> ValidadorLugar.validar("n".repeat(150), DIRECCION, null, null));
        rechazar("n".repeat(151), DIRECCION, null, null);
    }

    @Test
    @DisplayName("La dirección es obligatoria")
    void direccion_obligatoria() {
        assertEquals("La dirección del lugar es obligatoria.", rechazar(NOMBRE, null, null, null).getMessage());
        assertEquals("La dirección del lugar es obligatoria.", rechazar(NOMBRE, " ", null, null).getMessage());
    }

    @Test
    @DisplayName("La dirección admite 255 caracteres y rechaza 256")
    void direccion_larga() {
        assertDoesNotThrow(() -> ValidadorLugar.validar(NOMBRE, "d".repeat(255), null, null));
        rechazar(NOMBRE, "d".repeat(256), null, null);
    }

    @Test
    @DisplayName("La latitud y la longitud deben indicarse juntas")
    void coordenadas_incompletas() {
        assertEquals("La latitud y la longitud deben indicarse juntas.",
                rechazar(NOMBRE, DIRECCION, 4.3, null).getMessage());
        assertEquals("La latitud y la longitud deben indicarse juntas.",
                rechazar(NOMBRE, DIRECCION, null, -74.3).getMessage());
    }

    @Test
    @DisplayName("La latitud debe estar entre -90 y 90")
    void latitud_fuera_de_rango() {
        assertDoesNotThrow(() -> ValidadorLugar.validar(NOMBRE, DIRECCION, 90.0, 0.0));
        assertDoesNotThrow(() -> ValidadorLugar.validar(NOMBRE, DIRECCION, -90.0, 0.0));
        assertEquals("La latitud debe estar entre -90 y 90.", rechazar(NOMBRE, DIRECCION, 90.1, 0.0).getMessage());
        assertEquals("La latitud debe estar entre -90 y 90.", rechazar(NOMBRE, DIRECCION, -90.1, 0.0).getMessage());
    }

    @Test
    @DisplayName("La longitud debe estar entre -180 y 180")
    void longitud_fuera_de_rango() {
        assertDoesNotThrow(() -> ValidadorLugar.validar(NOMBRE, DIRECCION, 0.0, 180.0));
        assertDoesNotThrow(() -> ValidadorLugar.validar(NOMBRE, DIRECCION, 0.0, -180.0));
        assertEquals("La longitud debe estar entre -180 y 180.", rechazar(NOMBRE, DIRECCION, 0.0, 180.1).getMessage());
        assertEquals("La longitud debe estar entre -180 y 180.", rechazar(NOMBRE, DIRECCION, 0.0, -180.1).getMessage());
    }

    @Test
    @DisplayName("Una coordenada que no es un número (NaN) se rechaza")
    void coordenada_nan() {
        rechazar(NOMBRE, DIRECCION, Double.NaN, 0.0);
        rechazar(NOMBRE, DIRECCION, 0.0, Double.NaN);
    }
}