package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorProductoTest {

    private static final String NOMBRE = "Bolso tejido";
    private static final BigDecimal PRECIO = new BigDecimal("50000");

    private DatoInvalidoException rechazar(String nombre, BigDecimal precio, Integer cantidad) {
        return assertThrows(DatoInvalidoException.class,
                () -> ValidadorProducto.validar(nombre, precio, cantidad));
    }

    @Test
    @DisplayName("Un producto con datos correctos se acepta")
    void producto_valido() {
        assertDoesNotThrow(() -> ValidadorProducto.validar(NOMBRE, PRECIO, 3));
    }

    @Test
    @DisplayName("La cantidad puede ser cero (producto agotado)")
    void cantidad_cero() {
        assertDoesNotThrow(() -> ValidadorProducto.validar(NOMBRE, PRECIO, 0));
    }

    @Test
    @DisplayName("El nombre es obligatorio")
    void nombre_obligatorio() {
        assertEquals("El nombre del producto es obligatorio.", rechazar(null, PRECIO, 1).getMessage());
        assertEquals("El nombre del producto es obligatorio.", rechazar("   ", PRECIO, 1).getMessage());
    }

    @Test
    @DisplayName("El nombre admite 150 caracteres y rechaza 151")
    void nombre_largo() {
        assertDoesNotThrow(() -> ValidadorProducto.validar("n".repeat(150), PRECIO, 1));
        rechazar("n".repeat(151), PRECIO, 1);
    }

    @Test
    @DisplayName("El precio es obligatorio")
    void precio_obligatorio() {
        assertEquals("Indica el precio del producto.", rechazar(NOMBRE, null, 1).getMessage());
    }

    @Test
    @DisplayName("El precio debe ser mayor a cero")
    void precio_positivo() {
        assertEquals("El precio debe ser mayor a 0.", rechazar(NOMBRE, BigDecimal.ZERO, 1).getMessage());
        assertEquals("El precio debe ser mayor a 0.", rechazar(NOMBRE, new BigDecimal("-5"), 1).getMessage());
    }

    @Test
    @DisplayName("El precio admite como máximo 2 decimales, y 12.500 cuenta como 12.5")
    void precio_decimales() {
        assertDoesNotThrow(() -> ValidadorProducto.validar(NOMBRE, new BigDecimal("12.50"), 1));
        assertDoesNotThrow(() -> ValidadorProducto.validar(NOMBRE, new BigDecimal("12.500"), 1));
        assertEquals("El precio admite como máximo 2 decimales.",
                rechazar(NOMBRE, new BigDecimal("12.505"), 1).getMessage());
    }

    @Test
    @DisplayName("El precio no puede exceder lo que cabe en la base de datos")
    void precio_maximo() {
        assertDoesNotThrow(() -> ValidadorProducto.validar(NOMBRE, new BigDecimal("99999999.99"), 1));
        assertEquals("El precio es demasiado alto.",
                rechazar(NOMBRE, new BigDecimal("100000000"), 1).getMessage());
    }

    @Test
    @DisplayName("La cantidad es obligatoria")
    void cantidad_obligatoria() {
        assertEquals("Indica la cantidad disponible.", rechazar(NOMBRE, PRECIO, null).getMessage());
    }

    @Test
    @DisplayName("La cantidad no puede ser negativa")
    void cantidad_negativa() {
        assertEquals("La cantidad no puede ser negativa.", rechazar(NOMBRE, PRECIO, -1).getMessage());
    }

    @Test
    @DisplayName("La cantidad admite hasta un millón de unidades")
    void cantidad_maxima() {
        assertDoesNotThrow(() -> ValidadorProducto.validar(NOMBRE, PRECIO, 1_000_000));
        rechazar(NOMBRE, PRECIO, 1_000_001);
    }
}