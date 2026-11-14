package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.AsistenteNoDisponibleException;
import com.sgfa.backend.application.exception.LimiteUsoAsistenteException;
import com.sgfa.backend.application.port.in.AnalizarSolicitudesUseCase;
import com.sgfa.backend.domain.model.AnalisisSolicitudes;
import com.sgfa.backend.domain.model.PrioridadSolicitud;
import com.sgfa.backend.domain.model.SugerenciaSolicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AsistenteControllerTest {

    private AnalizarSolicitudesUseCase analizar;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        analizar = mock(AnalizarSolicitudesUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new AsistenteController(analizar))
                .setControllerAdvice(new ManejadorGlobalErrores())
                .build();
    }

    private org.springframework.test.web.servlet.ResultActions pedir(String cuerpo) throws Exception {
        return mvc.perform(post("/api/asistente/solicitudes/analisis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    @Test
    @DisplayName("POST /api/asistente/solicitudes/analisis devuelve el análisis")
    void devuelve_el_analisis() throws Exception {
        when(analizar.analizar(5L)).thenReturn(new AnalisisSolicitudes(5L, "Resumen del asistente", 3, 4, 2,
                "Gemini", List.of(new SugerenciaSolicitud(11L, PrioridadSolicitud.ALTA, "Buen catálogo"))));

        pedir("{\"idEdicionFeria\":5}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEdicionFeria").value(5))
                .andExpect(jsonPath("$.resumen").value("Resumen del asistente"))
                .andExpect(jsonPath("$.cuposDisponibles").value(3))
                .andExpect(jsonPath("$.totalPendientes").value(4))
                .andExpect(jsonPath("$.analizadas").value(2))
                .andExpect(jsonPath("$.proveedor").value("Gemini"))
                .andExpect(jsonPath("$.sugerencias[0].idInscripcion").value(11))
                .andExpect(jsonPath("$.sugerencias[0].prioridad").value("ALTA"))
                .andExpect(jsonPath("$.sugerencias[0].motivo").value("Buen catálogo"));
    }

    @Test
    @DisplayName("acepta el id como texto numérico")
    void id_como_texto() throws Exception {
        when(analizar.analizar(5L)).thenReturn(new AnalisisSolicitudes(5L, "ok", null, 0, 0, "Gemini", List.of()));

        pedir("{\"idEdicionFeria\":\"5\"}").andExpect(status().isOk());
    }

    @Test
    @DisplayName("sin idEdicionFeria responde 400")
    void sin_id() throws Exception {
        pedir("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Indica la edición que quieres analizar."));
    }

    @Test
    @DisplayName("un id que no es número responde 400")
    void id_invalido() throws Exception {
        pedir("{\"idEdicionFeria\":\"abc\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La edición indicada no es válida."));
    }

    @Test
    @DisplayName("una edición que no existe responde 404")
    void edicion_inexistente() throws Exception {
        when(analizar.analizar(99L)).thenThrow(new IllegalArgumentException("La edición no existe."));

        pedir("{\"idEdicionFeria\":99}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La edición no existe."));
    }

    @Test
    @DisplayName("si se superó el límite de uso responde 429")
    void limite_de_uso() throws Exception {
        when(analizar.analizar(5L)).thenThrow(new LimiteUsoAsistenteException("Espera 30 segundos."));

        pedir("{\"idEdicionFeria\":5}")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Espera 30 segundos."));
    }

    @Test
    @DisplayName("si el asistente no está disponible responde 503")
    void asistente_no_disponible() throws Exception {
        when(analizar.analizar(5L)).thenThrow(new AsistenteNoDisponibleException("Sin conexión con la IA."));

        pedir("{\"idEdicionFeria\":5}")
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Sin conexión con la IA."));
    }
}
