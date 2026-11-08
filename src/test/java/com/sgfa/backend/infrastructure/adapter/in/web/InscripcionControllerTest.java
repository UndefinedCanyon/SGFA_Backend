package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.CupoAgotadoException;
import com.sgfa.backend.application.exception.InscripcionDuplicadaException;
import com.sgfa.backend.application.port.in.AprobarSolicitudUseCase;
import com.sgfa.backend.application.port.in.ConsultarSolicitudesUseCase;
import com.sgfa.backend.application.port.in.RechazarSolicitudUseCase;
import com.sgfa.backend.application.port.in.SolicitarParticipacionUseCase;
import com.sgfa.backend.domain.model.Inscripcion;
import com.sgfa.backend.infrastructure.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InscripcionControllerTest {

    private SolicitarParticipacionUseCase solicitar;
    private AprobarSolicitudUseCase aprobar;
    private RechazarSolicitudUseCase rechazar;
    private ConsultarSolicitudesUseCase consultar;
    private MockMvc mvc;

    private static void iniciarSesion(long id, String rol) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(id, "usuario@correo.com", rol), null, List.of()));
    }

    private static Inscripcion inscripcion(long id, long idArtesano, long idEdicion, String estado) {
        return new Inscripcion(id, idArtesano, idEdicion, null, estado);
    }

    @BeforeEach
    void preparar() {
        SecurityContextHolder.clearContext();
        solicitar = mock(SolicitarParticipacionUseCase.class);
        aprobar = mock(AprobarSolicitudUseCase.class);
        rechazar = mock(RechazarSolicitudUseCase.class);
        consultar = mock(ConsultarSolicitudesUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new InscripcionController(solicitar, aprobar, rechazar, consultar))
                .setControllerAdvice(new ManejadorGlobalErrores())
                .build();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/inscripciones solicita participar usando al artesano de la sesión")
    void solicitar_participacion() throws Exception {
        iniciarSesion(7L, "ARTESANO");
        when(solicitar.solicitar(7L, 4L)).thenReturn(inscripcion(1L, 7L, 4L, "PENDIENTE"));

        mvc.perform(post("/api/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEdicionFeria\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.idArtesano").value(7))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("el artesano sale de la sesión, no de lo que mande el cliente")
    void el_artesano_no_se_toma_del_cuerpo() throws Exception {
        iniciarSesion(7L, "ARTESANO");
        when(solicitar.solicitar(7L, 4L)).thenReturn(inscripcion(1L, 7L, 4L, "PENDIENTE"));

        mvc.perform(post("/api/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEdicionFeria\":4,\"idArtesano\":999}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idArtesano").value(7));
    }

    @Test
    @DisplayName("GET /api/inscripciones/mias lista las solicitudes del artesano de la sesión")
    void consultar_mis_solicitudes() throws Exception {
        iniciarSesion(7L, "ARTESANO");
        when(consultar.consultarPorArtesano(7L)).thenReturn(List.of(
                inscripcion(1L, 7L, 4L, "PENDIENTE"), inscripcion(2L, 7L, 5L, "APROBADA")));

        mvc.perform(get("/api/inscripciones/mias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].estado").value("APROBADA"));
    }

    @Test
    @DisplayName("GET /api/inscripciones lista todas las solicitudes")
    void consultar_todas() throws Exception {
        when(consultar.consultarTodas()).thenReturn(List.of(inscripcion(1L, 7L, 4L, "PENDIENTE")));

        mvc.perform(get("/api/inscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /{id}/aprobar aprueba la solicitud")
    void aprobar_una_solicitud() throws Exception {
        when(aprobar.aprobar(3L)).thenReturn(inscripcion(3L, 7L, 4L, "APROBADA"));

        mvc.perform(put("/api/inscripciones/3/aprobar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    @Test
    @DisplayName("PUT /{id}/rechazar rechaza la solicitud")
    void rechazar_una_solicitud() throws Exception {
        when(rechazar.rechazar(3L)).thenReturn(inscripcion(3L, 7L, 4L, "RECHAZADA"));

        mvc.perform(put("/api/inscripciones/3/rechazar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
    }

    @Test
    @DisplayName("solicitar dos veces la misma edición responde 409")
    void inscripcion_duplicada_responde_409() throws Exception {
        iniciarSesion(7L, "ARTESANO");
        when(solicitar.solicitar(7L, 4L)).thenThrow(new InscripcionDuplicadaException("Ya solicitaste esta feria."));

        mvc.perform(post("/api/inscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEdicionFeria\":4}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ya solicitaste esta feria."));
    }

    @Test
    @DisplayName("aprobar sin cupo disponible responde 409")
    void cupo_agotado_responde_409() throws Exception {
        when(aprobar.aprobar(3L)).thenThrow(new CupoAgotadoException("No quedan cupos."));

        mvc.perform(put("/api/inscripciones/3/aprobar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("No quedan cupos."));
    }

    @Test
    @DisplayName("una solicitud que no existe responde 404")
    void solicitud_inexistente_responde_404() throws Exception {
        when(rechazar.rechazar(99L)).thenThrow(new IllegalArgumentException("La solicitud no existe."));

        mvc.perform(put("/api/inscripciones/99/rechazar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("La solicitud no existe."));
    }
}