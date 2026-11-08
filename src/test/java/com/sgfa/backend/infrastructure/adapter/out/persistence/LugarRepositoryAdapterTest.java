package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.Lugar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LugarRepositoryAdapterTest extends PersistenciaTestBase {

    @Test
    @DisplayName("guarda un lugar con coordenadas y lo recupera")
    void guardar_con_coordenadas() {
        Lugar guardado = nuevoLugar();
        sincronizar();

        assertNotNull(guardado.getId());
        Lugar leido = lugares.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("Plaza Mayor", leido.getNombre());
        assertEquals("Calle 1 # 2-3", leido.getDireccion());
        assertEquals(4.3369, leido.getLatitud().doubleValue(), 0.00001);
        assertEquals(-74.3636, leido.getLongitud().doubleValue(), 0.00001);
        assertTrue(leido.isActivo());
    }

    @Test
    @DisplayName("un lugar puede guardarse sin coordenadas")
    void guardar_sin_coordenadas() {
        Lugar guardado = lugares.guardar(new Lugar(null, "Por definir", "Sin direccion", null, null, false));
        sincronizar();

        Lugar leido = lugares.buscarPorId(guardado.getId()).orElseThrow();
        assertNull(leido.getLatitud());
        assertNull(leido.getLongitud());
        assertFalse(leido.isActivo());
    }

    @Test
    @DisplayName("lista todos los lugares y devuelve vacío si el id no existe")
    void listar_y_buscar_inexistente() {
        nuevoLugar();
        nuevoLugar();

        assertEquals(2, lugares.listarTodos().size());
        assertTrue(lugares.buscarPorId(9999L).isEmpty());
    }
}