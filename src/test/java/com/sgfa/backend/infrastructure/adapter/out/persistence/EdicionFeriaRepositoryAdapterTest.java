package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Feria;
import com.sgfa.backend.domain.model.Lugar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EdicionFeriaRepositoryAdapterTest extends PersistenciaTestBase {

    private Feria feria;
    private Lugar lugar;

    @BeforeEach
    void preparar() {
        feria = nuevaFeria("Feria Andina", true);
        lugar = nuevoLugar();
    }

    @Test
    @DisplayName("guarda la edición con sus fechas y su cupo")
    void guardar_y_buscar() {
        EdicionFeria guardada = nuevaEdicion(feria.getId(), lugar.getId(), true, 30);
        sincronizar();

        assertNotNull(guardada.getId());
        EdicionFeria leida = ediciones.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals(feria.getId(), leida.getIdFeria());
        assertEquals(lugar.getId(), leida.getIdLugar());
        assertEquals(LocalDate.of(2026, 12, 1), leida.getFechaInicio());
        assertEquals(LocalDate.of(2026, 12, 3), leida.getFechaFin());
        assertEquals(30, leida.getCupoMaximo());
        assertTrue(leida.isActivo());
    }

    @Test
    @DisplayName("el cupo máximo puede quedar sin límite")
    void cupo_sin_limite() {
        EdicionFeria guardada = nuevaEdicion(feria.getId(), lugar.getId(), true, null);
        sincronizar();

        assertNull(ediciones.buscarPorId(guardada.getId()).orElseThrow().getCupoMaximo());
    }

    @Test
    @DisplayName("no guarda una edición de una feria que no existe")
    void feria_inexistente() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> nuevaEdicion(9999L, lugar.getId(), true, null));

        assertEquals("La feria indicada no existe.", error.getMessage());
    }

    @Test
    @DisplayName("no guarda una edición en un lugar que no existe")
    void lugar_inexistente() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> nuevaEdicion(feria.getId(), 9999L, true, null));

        assertEquals("El lugar indicado no existe.", error.getMessage());
    }

    @Test
    @DisplayName("listarTodas devuelve solo las activas y la otra variante devuelve todas")
    void listar_activas_y_todas() {
        nuevaEdicion(feria.getId(), lugar.getId(), true, null);
        nuevaEdicion(feria.getId(), lugar.getId(), false, null);

        assertEquals(1, ediciones.listarTodas().size());
        assertEquals(2, ediciones.listarTodasIncluyendoInactivas().size());
    }

    @Test
    @DisplayName("listarPorFeria filtra por feria y ignora las inactivas")
    void listar_por_feria() {
        Feria otra = nuevaFeria("Feria Navidena", true);
        nuevaEdicion(feria.getId(), lugar.getId(), true, null);
        nuevaEdicion(feria.getId(), lugar.getId(), false, null);
        nuevaEdicion(otra.getId(), lugar.getId(), true, null);

        assertEquals(1, ediciones.listarPorFeria(feria.getId()).size());
        assertEquals(1, ediciones.listarPorFeria(otra.getId()).size());
        assertTrue(ediciones.listarPorFeria(9999L).isEmpty());
    }

    @Test
    @DisplayName("cambiar el estado conserva el cupo y las fechas")
    void cambiar_estado() {
        EdicionFeria guardada = nuevaEdicion(feria.getId(), lugar.getId(), true, 25);

        ediciones.cambiarEstadoActivo(guardada.getId(), false);
        sincronizar();

        EdicionFeria leida = ediciones.buscarPorId(guardada.getId()).orElseThrow();
        assertFalse(leida.isActivo());
        assertEquals(25, leida.getCupoMaximo());
        assertEquals(LocalDate.of(2026, 12, 1), leida.getFechaInicio());
    }

    @Test
    @DisplayName("cambiar el estado de una edición que no existe lanza un error claro")
    void cambiar_estado_de_inexistente() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ediciones.cambiarEstadoActivo(9999L, true));

        assertEquals("La edición no existe.", error.getMessage());
    }

    @Test
    @DisplayName("buscar una edición que no existe devuelve vacío")
    void buscar_inexistente() {
        assertTrue(ediciones.buscarPorId(9999L).isEmpty());
    }
}