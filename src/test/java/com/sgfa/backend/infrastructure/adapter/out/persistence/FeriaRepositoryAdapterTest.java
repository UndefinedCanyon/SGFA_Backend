package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.Feria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeriaRepositoryAdapterTest extends PersistenciaTestBase {

    @Test
    @DisplayName("guarda la feria y la recupera por id")
    void guardar_y_buscar() {
        Feria guardada = nuevaFeria("Feria Andina", true);
        sincronizar();

        assertNotNull(guardada.getId());
        Feria leida = ferias.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals("Feria Andina", leida.getNombreFeria());
        assertEquals(Long.valueOf(1L), Long.valueOf(leida.getIdAdmin()));
        assertTrue(leida.isActivo());
        assertTrue(ferias.buscarPorId(9999L).isEmpty());
    }

    @Test
    @DisplayName("listarTodas devuelve solo las activas")
    void listar_solo_activas() {
        nuevaFeria("Activa", true);
        nuevaFeria("Inactiva", false);

        assertEquals(1, ferias.listarTodas().size());
        assertEquals("Activa", ferias.listarTodas().get(0).getNombreFeria());
    }

    @Test
    @DisplayName("listarTodasIncluyendoInactivas devuelve todas")
    void listar_incluyendo_inactivas() {
        nuevaFeria("Activa", true);
        nuevaFeria("Inactiva", false);

        assertEquals(2, ferias.listarTodasIncluyendoInactivas().size());
    }

    @Test
    @DisplayName("cambiar el estado activa o desactiva la feria")
    void cambiar_estado() {
        Feria guardada = nuevaFeria("Feria Andina", true);

        ferias.cambiarEstadoActivo(guardada.getId(), false);
        sincronizar();

        assertFalse(ferias.buscarPorId(guardada.getId()).orElseThrow().isActivo());
    }

    @Test
    @DisplayName("cambiar el estado de una feria que no existe lanza un error claro")
    void cambiar_estado_de_inexistente() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ferias.cambiarEstadoActivo(9999L, true));

        assertEquals("La feria no existe.", error.getMessage());
    }
}