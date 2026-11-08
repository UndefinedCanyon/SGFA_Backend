package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Inscripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InscripcionRepositoryAdapterTest extends PersistenciaTestBase {

    @Autowired
    private InscripcionRepositoryAdapter inscripciones;

    private Artesano ana;
    private Artesano luis;
    private EdicionFeria edicion;

    @BeforeEach
    void preparar() {
        ana = nuevoArtesano("ana@correo.com", "1010");
        luis = nuevoArtesano("luis@correo.com", "2020");
        edicion = nuevaEdicion(nuevaFeria("Feria Andina", true).getId(), nuevoLugar().getId(), true, null);
    }

    private Inscripcion inscribir(Artesano artesano, String estado) {
        return inscripciones.guardar(
                new Inscripcion(null, artesano.getId(), edicion.getId(), LocalDate.of(2026, 11, 1), estado));
    }

    @Test
    @DisplayName("guarda la inscripción y la recupera con su estado y fecha")
    void guardar_y_buscar() {
        Inscripcion guardada = inscribir(ana, "PENDIENTE");
        sincronizar();

        assertNotNull(guardada.getId());
        Inscripcion leida = inscripciones.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals(ana.getId(), leida.getIdArtesano());
        assertEquals(edicion.getId(), leida.getIdEdicionFeria());
        assertEquals(LocalDate.of(2026, 11, 1), leida.getFechaInscripcion());
        assertEquals("PENDIENTE", leida.getEstado());
        assertTrue(inscripciones.buscarPorId(9999L).isEmpty());
    }

    @Test
    @DisplayName("no guarda si el artesano o la edición no existen")
    void referencias_inexistentes() {
        IllegalArgumentException sinArtesano = assertThrows(IllegalArgumentException.class,
                () -> inscripciones.guardar(
                        new Inscripcion(null, 9999L, edicion.getId(), LocalDate.of(2026, 11, 1), "PENDIENTE")));
        IllegalArgumentException sinEdicion = assertThrows(IllegalArgumentException.class,
                () -> inscripciones.guardar(
                        new Inscripcion(null, ana.getId(), 9999L, LocalDate.of(2026, 11, 1), "PENDIENTE")));

        assertEquals("El artesano indicado no existe.", sinArtesano.getMessage());
        assertEquals("La edición de feria indicada no existe.", sinEdicion.getMessage());
    }

    @Test
    @DisplayName("lista las inscripciones de un artesano y todas las inscripciones")
    void listar() {
        inscribir(ana, "PENDIENTE");
        inscribir(luis, "APROBADA");

        assertEquals(1, inscripciones.listarPorArtesano(ana.getId()).size());
        assertEquals(2, inscripciones.listarTodas().size());
    }

    @Test
    @DisplayName("sabe si el artesano ya se inscribió a la edición")
    void existe_inscripcion() {
        inscribir(ana, "PENDIENTE");

        assertTrue(inscripciones.existeInscripcion(ana.getId(), edicion.getId()));
        assertFalse(inscripciones.existeInscripcion(luis.getId(), edicion.getId()));
    }

    @Test
    @DisplayName("lista y cuenta solo las inscripciones aprobadas de la edición")
    void aprobadas_por_edicion() {
        inscribir(ana, "APROBADA");
        inscribir(luis, "RECHAZADA");

        assertEquals(1, inscripciones.listarAprobadasPorEdicion(edicion.getId()).size());
        assertEquals(1, inscripciones.contarAprobadasPorEdicion(edicion.getId()));
        assertEquals(0, inscripciones.contarAprobadasPorEdicion(9999L));
    }

    @Test
    @DisplayName("la base impide inscribir dos veces al mismo artesano en la misma edición")
    void inscripcion_unica() {
        inscribir(ana, "PENDIENTE");

        assertThrows(DataIntegrityViolationException.class, () -> inscribir(ana, "PENDIENTE"));
    }
}