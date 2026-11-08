package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarLugarUseCase;
import com.sgfa.backend.application.port.in.CambiarEstadoLugarUseCase;
import com.sgfa.backend.application.port.in.ConsultarLugaresUseCase;
import com.sgfa.backend.application.port.in.CrearLugarUseCase;
import com.sgfa.backend.domain.model.Lugar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LugarControllerTest {

    private CrearLugarUseCase crear;
    private ActualizarLugarUseCase actualizar;
    private CambiarEstadoLugarUseCase cambiarEstado;
    private ConsultarLugaresUseCase consultar;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        crear = mock(CrearLugarUseCase.class);
        actualizar = mock(ActualizarLugarUseCase.class);
        cambiarEstado = mock(CambiarEstadoLugarUseCase.class);
        consultar = mock(ConsultarLugaresUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new LugarController(crear, actualizar, cambiarEstado, consultar))
                .build();
    }

    @Test
    @DisplayName("POST /api/lugares crea el lugar con sus coordenadas")
    void crear_un_lugar() throws Exception {
        when(crear.crear("Parque Principal", "Calle 6 #6-30", 4.3369, -74.3636))
                .thenReturn(new Lugar(1L, "Parque Principal", "Calle 6 #6-30", 4.3369, -74.3636));

        mvc.perform(post("/api/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Parque Principal\",\"direccion\":\"Calle 6 #6-30\","
                                + "\"latitud\":4.3369,\"longitud\":-74.3636}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Parque Principal"))
                .andExpect(jsonPath("$.latitud").value(4.3369))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("las coordenadas son opcionales y llegan como nulas")
    void las_coordenadas_pueden_faltar() throws Exception {
        when(crear.crear("Plaza", "Carrera 7", null, null))
                .thenReturn(new Lugar(2L, "Plaza", "Carrera 7", null, null));

        mvc.perform(post("/api/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Plaza\",\"direccion\":\"Carrera 7\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Plaza"));

        verify(crear).crear("Plaza", "Carrera 7", null, null);
    }

    @Test
    @DisplayName("coordenadas que no son números responden 400 sin crear nada")
    void coordenadas_no_numericas_responden_400() throws Exception {
        mvc.perform(post("/api/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Plaza\",\"direccion\":\"Carrera 7\","
                                + "\"latitud\":\"norte\",\"longitud\":-74.3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());

        verifyNoInteractions(crear);
    }

    @Test
    @DisplayName("PUT /api/lugares/{id} actualiza el lugar")
    void actualizar_un_lugar() throws Exception {
        when(actualizar.actualizar(4L, "Plaza Nueva", "Calle 1", 4.1, -74.1))
                .thenReturn(new Lugar(4L, "Plaza Nueva", "Calle 1", 4.1, -74.1));

        mvc.perform(put("/api/lugares/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Plaza Nueva\",\"direccion\":\"Calle 1\","
                                + "\"latitud\":4.1,\"longitud\":-74.1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Plaza Nueva"));
    }

    @Test
    @DisplayName("actualizar un lugar que no existe responde 404")
    void actualizar_un_lugar_inexistente_responde_404() throws Exception {
        when(actualizar.actualizar(99L, "X", "Y", null, null))
                .thenThrow(new IllegalArgumentException("El lugar no existe."));

        mvc.perform(put("/api/lugares/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X\",\"direccion\":\"Y\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El lugar no existe."));
    }

    @Test
    @DisplayName("un dato inválido del caso de uso responde 400")
    void dato_invalido_responde_400() throws Exception {
        when(crear.crear("", "Calle 1", null, null))
                .thenThrow(new DatoInvalidoException("El nombre del lugar es obligatorio."));

        mvc.perform(post("/api/lugares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"direccion\":\"Calle 1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El nombre del lugar es obligatorio."));
    }

    @Test
    @DisplayName("PUT /api/lugares/{id}/estado activa o desactiva el lugar")
    void cambiar_el_estado() throws Exception {
        mvc.perform(put("/api/lugares/4/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk());

        verify(cambiarEstado).cambiarEstado(4L, false);
    }

    @Test
    @DisplayName("si «activo» no es verdadero o falso responde 400 sin cambiar nada")
    void estado_que_no_es_booleano_responde_400() throws Exception {
        mvc.perform(put("/api/lugares/4/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":\"si\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());

        mvc.perform(put("/api/lugares/4/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(cambiarEstado, never()).cambiarEstado(4L, true);
        verify(cambiarEstado, never()).cambiarEstado(4L, false);
    }

    @Test
    @DisplayName("GET /api/lugares lista los lugares")
    void listar_los_lugares() throws Exception {
        when(consultar.consultarTodos()).thenReturn(List.of(
                new Lugar(1L, "Parque", "Calle 1", 4.0, -74.0),
                new Lugar(2L, "Plaza", "Calle 2", 4.1, -74.1, false)));

        mvc.perform(get("/api/lugares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Plaza"))
                .andExpect(jsonPath("$[1].activo").value(false));
    }
}