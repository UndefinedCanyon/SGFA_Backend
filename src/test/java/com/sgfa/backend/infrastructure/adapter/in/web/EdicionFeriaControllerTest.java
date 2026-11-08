package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.CambiarEstadoEdicionFeriaUseCase;
import com.sgfa.backend.application.port.in.ConfigurarCupoEdicionUseCase;
import com.sgfa.backend.application.port.in.ConsultarEdicionesFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarOcupacionEdicionUseCase;
import com.sgfa.backend.application.port.in.ConsultarParticipantesEdicionUseCase;
import com.sgfa.backend.application.port.in.CrearEdicionFeriaUseCase;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.OcupacionEdicion;
import com.sgfa.backend.domain.model.ParticipantePublico;
import com.sgfa.backend.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EdicionFeriaControllerTest {

    private CrearEdicionFeriaUseCase crear;
    private ConsultarEdicionesFeriaUseCase consultar;
    private CambiarEstadoEdicionFeriaUseCase cambiarEstado;
    private ConsultarParticipantesEdicionUseCase participantes;
    private ConfigurarCupoEdicionUseCase configurarCupo;
    private ConsultarOcupacionEdicionUseCase ocupacion;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        crear = mock(CrearEdicionFeriaUseCase.class);
        consultar = mock(ConsultarEdicionesFeriaUseCase.class);
        cambiarEstado = mock(CambiarEstadoEdicionFeriaUseCase.class);
        participantes = mock(ConsultarParticipantesEdicionUseCase.class);
        configurarCupo = mock(ConfigurarCupoEdicionUseCase.class);
        ocupacion = mock(ConsultarOcupacionEdicionUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new EdicionFeriaController(
                crear, consultar, cambiarEstado, participantes, configurarCupo, ocupacion)).build();
    }

    private static EdicionFeria edicion(long id, boolean activo, Integer cupo) {
        return new EdicionFeria(id, 1L, 2L, null, null, activo, cupo);
    }

    @Test
    @DisplayName("POST /api/edicionesferia crea la edición con sus fechas")
    void crear_una_edicion() throws Exception {
        when(crear.crear(1L, 2L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3)))
                .thenReturn(edicion(10L, true, null));

        mvc.perform(post("/api/edicionesferia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idFeria\":1,\"idLugar\":2,\"fechaInicio\":\"2026-12-01\",\"fechaFin\":\"2026-12-03\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.activo").value(true));

        verify(crear).crear(1L, 2L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3));
    }

    @Test
    @DisplayName("GET /api/edicionesferia sin filtro lista las ediciones activas")
    void listar_sin_filtro() throws Exception {
        when(consultar.consultarTodas()).thenReturn(List.of(edicion(1L, true, 30), edicion(2L, true, null)));

        mvc.perform(get("/api/edicionesferia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cupoMaximo").value(30));

        verify(consultar, never()).consultarPorFeria(1L);
    }

    @Test
    @DisplayName("GET /api/edicionesferia?idFeria=5 filtra por feria")
    void listar_por_feria() throws Exception {
        when(consultar.consultarPorFeria(5L)).thenReturn(List.of(edicion(3L, true, null)));

        mvc.perform(get("/api/edicionesferia").param("idFeria", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));

        verify(consultar, never()).consultarTodas();
    }

    @Test
    @DisplayName("GET /api/edicionesferia/todas incluye las inactivas")
    void listar_todas_incluyendo_inactivas() throws Exception {
        when(consultar.consultarTodasIncluyendoInactivas())
                .thenReturn(List.of(edicion(1L, true, null), edicion(2L, false, null)));

        mvc.perform(get("/api/edicionesferia/todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].activo").value(false));
    }

    @Test
    @DisplayName("GET /{id}/participantes devuelve los participantes con sus productos")
    void listar_participantes() throws Exception {
        Producto vasija = new Producto(1L, "Vasija", new BigDecimal("25000"), 4, 7L);
        when(participantes.consultar(4L)).thenReturn(List.of(
                new ParticipantePublico(7L, "Cerámicas Ana", "Vasijas a mano", "logo.png", List.of(vasija))));

        mvc.perform(get("/api/edicionesferia/4/participantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreEmprendimiento").value("Cerámicas Ana"))
                .andExpect(jsonPath("$[0].productos[0].nombre").value("Vasija"));
    }

    @Test
    @DisplayName("GET /{id}/ocupacion informa el cupo, los aprobados y los disponibles")
    void consultar_la_ocupacion() throws Exception {
        when(ocupacion.consultar(4L)).thenReturn(new OcupacionEdicion(30, 12));

        mvc.perform(get("/api/edicionesferia/4/ocupacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cupoMaximo").value(30))
                .andExpect(jsonPath("$.aprobados").value(12))
                .andExpect(jsonPath("$.disponibles").value(18));
    }

    @Test
    @DisplayName("una edición sin cupo máximo no tiene cupos disponibles calculados")
    void ocupacion_sin_cupo_maximo() throws Exception {
        when(ocupacion.consultar(4L)).thenReturn(new OcupacionEdicion(null, 5));

        mvc.perform(get("/api/edicionesferia/4/ocupacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cupoMaximo").value(nullValue()))
                .andExpect(jsonPath("$.disponibles").value(nullValue()));
    }

    @Test
    @DisplayName("PUT /{id}/estado activa o desactiva la edición")
    void cambiar_el_estado() throws Exception {
        mvc.perform(put("/api/edicionesferia/4/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true}"))
                .andExpect(status().isOk());

        verify(cambiarEstado).cambiarEstado(4L, true);
    }

    @Test
    @DisplayName("PUT /{id}/cupo fija el cupo máximo")
    void configurar_el_cupo() throws Exception {
        when(configurarCupo.configurar(4L, 25)).thenReturn(edicion(4L, true, 25));

        mvc.perform(put("/api/edicionesferia/4/cupo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cupoMaximo\":25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cupoMaximo").value(25));
    }

    @Test
    @DisplayName("un cupo vacío o ausente quita el límite")
    void un_cupo_vacio_quita_el_limite() throws Exception {
        when(configurarCupo.configurar(4L, null)).thenReturn(edicion(4L, true, null));

        mvc.perform(put("/api/edicionesferia/4/cupo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cupoMaximo\":\"\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/edicionesferia/4/cupo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(configurarCupo, org.mockito.Mockito.times(2)).configurar(4L, null);
    }

    @Test
    @DisplayName("un dato inválido responde 400 con el mensaje")
    void dato_invalido_responde_400() throws Exception {
        when(configurarCupo.configurar(4L, -1)).thenThrow(new DatoInvalidoException("El cupo no puede ser negativo."));

        mvc.perform(put("/api/edicionesferia/4/cupo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cupoMaximo\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El cupo no puede ser negativo."));
    }

    @Test
    @DisplayName("una edición que no existe responde 404")
    void edicion_inexistente_responde_404() throws Exception {
        when(ocupacion.consultar(99L)).thenThrow(new IllegalArgumentException("La edición no existe."));

        mvc.perform(get("/api/edicionesferia/99/ocupacion"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La edición no existe."));
    }
}