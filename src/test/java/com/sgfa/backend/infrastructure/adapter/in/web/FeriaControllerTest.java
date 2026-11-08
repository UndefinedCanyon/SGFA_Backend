package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarFeriaUseCase;
import com.sgfa.backend.application.port.in.CambiarEstadoFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarFeriasUseCase;
import com.sgfa.backend.application.port.in.CrearFeriaUseCase;
import com.sgfa.backend.domain.model.Feria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeriaControllerTest {

    private CrearFeriaUseCase crear;
    private ConsultarFeriasUseCase consultar;
    private CambiarEstadoFeriaUseCase cambiarEstado;
    private ActualizarFeriaUseCase actualizar;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        crear = mock(CrearFeriaUseCase.class);
        consultar = mock(ConsultarFeriasUseCase.class);
        cambiarEstado = mock(CambiarEstadoFeriaUseCase.class);
        actualizar = mock(ActualizarFeriaUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new FeriaController(crear, consultar, cambiarEstado, actualizar))
                .build();
    }

    @Test
    @DisplayName("POST /api/ferias crea la feria")
    void crear_una_feria() throws Exception {
        when(crear.crear("Feria de la Cultura", 2L)).thenReturn(new Feria(1L, "Feria de la Cultura", 2L, true));

        mvc.perform(post("/api/ferias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreFeria\":\"Feria de la Cultura\",\"idAdmin\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreFeria").value("Feria de la Cultura"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("el id del administrador también se acepta como texto")
    void el_id_del_administrador_puede_venir_como_texto() throws Exception {
        when(crear.crear("Feria X", 5L)).thenReturn(new Feria(9L, "Feria X", 5L, true));

        mvc.perform(post("/api/ferias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreFeria\":\"Feria X\",\"idAdmin\":\"5\"}"))
                .andExpect(status().isOk());

        verify(crear).crear("Feria X", 5L);
    }

    @Test
    @DisplayName("GET /api/ferias lista las ferias activas")
    void listar_las_ferias_activas() throws Exception {
        when(consultar.consultarTodas()).thenReturn(List.of(new Feria(1L, "Feria A", 2L, true)));

        mvc.perform(get("/api/ferias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreFeria").value("Feria A"));
    }

    @Test
    @DisplayName("GET /api/ferias/todas incluye también las inactivas")
    void listar_todas_incluyendo_inactivas() throws Exception {
        when(consultar.consultarTodasIncluyendoInactivas()).thenReturn(List.of(
                new Feria(1L, "Feria A", 2L, true),
                new Feria(2L, "Feria B", 2L, false)));

        mvc.perform(get("/api/ferias/todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].activo").value(false));
    }

    @Test
    @DisplayName("PUT /api/ferias/{id} cambia el nombre de la feria")
    void actualizar_el_nombre() throws Exception {
        when(actualizar.actualizar(3L, "Nombre nuevo")).thenReturn(new Feria(3L, "Nombre nuevo", 2L, true));

        mvc.perform(put("/api/ferias/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreFeria\":\"Nombre nuevo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreFeria").value("Nombre nuevo"));
    }

    @Test
    @DisplayName("PUT /api/ferias/{id}/estado activa o desactiva la feria")
    void cambiar_el_estado() throws Exception {
        mvc.perform(put("/api/ferias/3/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk());

        verify(cambiarEstado).cambiarEstado(3L, false);
    }

    @Test
    @DisplayName("un dato inválido responde 400 con el mensaje")
    void dato_invalido_responde_400() throws Exception {
        when(crear.crear("", 2L)).thenThrow(new DatoInvalidoException("El nombre de la feria es obligatorio."));

        mvc.perform(post("/api/ferias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreFeria\":\"\",\"idAdmin\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El nombre de la feria es obligatorio."));
    }

    @Test
    @DisplayName("una feria que no existe responde 404")
    void feria_inexistente_responde_404() throws Exception {
        when(actualizar.actualizar(99L, "Nombre"))
                .thenThrow(new IllegalArgumentException("La feria no existe."));

        mvc.perform(put("/api/ferias/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreFeria\":\"Nombre\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La feria no existe."));
    }
}