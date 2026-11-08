package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.Artesano;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtesanoRepositoryAdapterTest extends PersistenciaTestBase {

    @Test
    @DisplayName("guarda al artesano, le asigna un id y conserva todos sus datos")
    void guardar_y_buscar_por_id() {
        Artesano guardado = nuevoArtesano("ana@correo.com", "1010");
        sincronizar();

        assertNotNull(guardado.getId());
        Artesano leido = artesanos.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("ana@correo.com", leido.getCorreoElectronico());
        assertEquals("1010", leido.getCc());
        assertEquals("Ceramicas Ana", leido.getNombreEmprendimiento());
        assertTrue(leido.isActivo());
        assertTrue(leido.isCorreoVerificado());
    }

    @Test
    @DisplayName("busca por correo y devuelve vacío si no existe")
    void buscar_por_correo() {
        nuevoArtesano("ana@correo.com", "1010");

        assertTrue(artesanos.buscarPorCorreo("ana@correo.com").isPresent());
        assertTrue(artesanos.buscarPorCorreo("nadie@correo.com").isEmpty());
        assertTrue(artesanos.buscarPorId(9999L).isEmpty());
    }

    @Test
    @DisplayName("lista a todos los artesanos")
    void listar_todos() {
        nuevoArtesano("ana@correo.com", "1010");
        nuevoArtesano("luis@correo.com", "2020");

        assertEquals(2, artesanos.listarTodos().size());
    }

    @Test
    @DisplayName("sabe si un correo o una cédula ya están registrados")
    void existe_por_correo_y_cc() {
        nuevoArtesano("ana@correo.com", "1010");

        assertTrue(artesanos.existePorCorreo("ana@correo.com"));
        assertFalse(artesanos.existePorCorreo("otro@correo.com"));
        assertTrue(artesanos.existePorCc("1010"));
        assertFalse(artesanos.existePorCc("9999"));
    }

    @Test
    @DisplayName("cambiar el estado desactiva la cuenta sin perder los demás datos")
    void cambiar_estado_activo() {
        Artesano guardado = nuevoArtesano("ana@correo.com", "1010");

        artesanos.cambiarEstadoActivo(guardado.getId(), false);
        sincronizar();

        Artesano leido = artesanos.buscarPorId(guardado.getId()).orElseThrow();
        assertFalse(leido.isActivo());
        assertEquals("ana@correo.com", leido.getCorreoElectronico());
        assertTrue(leido.isCorreoVerificado());
    }

    @Test
    @DisplayName("cambiar el estado de un artesano que no existe lanza un error claro")
    void cambiar_estado_de_inexistente() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> artesanos.cambiarEstadoActivo(9999L, false));

        assertEquals("El artesano no existe.", error.getMessage());
    }

    @Test
    @DisplayName("la base rechaza dos artesanos con el mismo correo")
    void correo_unico() {
        nuevoArtesano("ana@correo.com", "1010");

        assertThrows(DataIntegrityViolationException.class, () -> nuevoArtesano("ana@correo.com", "2020"));
    }
}