package com.sgfa.backend.infrastructure.adapter.out.ia;

import com.sgfa.backend.domain.model.ContextoEdicion;
import com.sgfa.backend.domain.model.PrioridadSolicitud;
import com.sgfa.backend.domain.model.ResultadoAsistente;
import com.sgfa.backend.domain.model.ResumenSolicitud;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsistenteIAFalsoAdapterTest {

    private final AsistenteIAFalsoAdapter asistente = new AsistenteIAFalsoAdapter();

    private static ContextoEdicion contexto(Integer cupoMaximo, Integer disponibles) {
        return new ContextoEdicion("Feria", "Parque", LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5),
                cupoMaximo, 1, disponibles, 3);
    }

    private static ResumenSolicitud solicitud(long id, String descripcion, String... productos) {
        return new ResumenSolicitud(id, "Taller " + id, descripcion, List.of(productos), LocalDate.of(2026, 10, 1));
    }

    @Test
    @DisplayName("se presenta como demostración, no como IA real")
    void proveedor() {
        assertEquals("Demostración", asistente.proveedor());
    }

    @Test
    @DisplayName("prioridad alta con dos o más productos y descripción clara; media con un producto o descripción corta; baja sin productos")
    void reglas() {
        ResultadoAsistente resultado = asistente.priorizarSolicitudes(contexto(5, 4), List.of(
                solicitud(1, "Tejidos de lana hechos a mano en telar", "Ruana", "Bufanda"),
                solicitud(2, "Tejidos de lana hechos a mano en telar", "Ruana"),
                solicitud(3, "Corta", "Ruana", "Bufanda"),
                solicitud(4, null, "Ruana"),
                solicitud(5, "Tejidos de lana hechos a mano en telar")));

        assertEquals(PrioridadSolicitud.ALTA, resultado.sugerencias().get(0).prioridad());
        assertEquals(PrioridadSolicitud.MEDIA, resultado.sugerencias().get(1).prioridad());
        assertEquals("Tiene un solo producto registrado.", resultado.sugerencias().get(1).motivo());
        assertEquals(PrioridadSolicitud.MEDIA, resultado.sugerencias().get(2).prioridad());
        assertEquals("Tiene productos, pero la descripción es corta o falta.", resultado.sugerencias().get(2).motivo());
        assertEquals(PrioridadSolicitud.MEDIA, resultado.sugerencias().get(3).prioridad());
        assertEquals(PrioridadSolicitud.BAJA, resultado.sugerencias().get(4).prioridad());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), resultado.sugerencias().stream().map(s -> s.idInscripcion()).toList());
    }

    @Test
    @DisplayName("el resumen aclara que es una demostración y menciona los cupos")
    void resumen_con_cupo() {
        ResultadoAsistente resultado = asistente.priorizarSolicitudes(contexto(5, 4), List.of(solicitud(1, "x", "a")));

        assertTrue(resultado.resumen().startsWith("Análisis de demostración (sin IA"));
        assertTrue(resultado.resumen().contains("Quedan 4 cupos para 3 solicitudes pendientes."));
    }

    @Test
    @DisplayName("sin límite de cupo lo dice")
    void resumen_sin_limite() {
        ResultadoAsistente resultado = asistente.priorizarSolicitudes(contexto(null, null), List.of(solicitud(1, "x", "a")));

        assertTrue(resultado.resumen().contains("no tiene límite de cupo"));
    }
}
