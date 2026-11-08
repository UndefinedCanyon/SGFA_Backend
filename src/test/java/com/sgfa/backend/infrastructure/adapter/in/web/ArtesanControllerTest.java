package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarPerfilArtesanoUseCase;
import com.sgfa.backend.application.port.in.CambiarEstadoArtesanoUseCase;
import com.sgfa.backend.application.port.in.ConsultarArtesanosUseCase;
import com.sgfa.backend.application.port.in.GestionarLogoArtesanoUseCase;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.infrastructure.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArtesanoControllerTest {

    private static final String CUERPO_REGISTRO = "{\"nombre\":\"Ana Gómez\",\"correoElectronico\":\"ana@correo.com\","
            + "\"contrasena\":\"Segura1234\",\"cc\":\"1010\",\"telefono\":\"300\","
            + "\"nombreEmprendimiento\":\"Cerámicas Ana\",\"descripcionCorta\":\"Vasijas a mano\"}";

    private RegistrarArtesanoUseCase registrar;
    private CambiarEstadoArtesanoUseCase cambiarEstado;
    private ConsultarArtesanosUseCase consultar;
    private ActualizarPerfilArtesanoUseCase actualizarPerfil;
    private GestionarLogoArtesanoUseCase logos;
    private MockMvc mvc;

    private static Artesano artesano(long id, boolean correoVerificado) {
        return new Artesano(id, "Ana Gómez", "ana@correo.com", "hash-secreto-no-publicable", "1010", "300",
                "Cerámicas Ana", "Vasijas a mano", true, "logo.png", correoVerificado);
    }

    @BeforeEach
    void preparar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(7L, "ana@correo.com", "ARTESANO"), null, List.of()));
        registrar = mock(RegistrarArtesanoUseCase.class);
        cambiarEstado = mock(CambiarEstadoArtesanoUseCase.class);
        consultar = mock(ConsultarArtesanosUseCase.class);
        actualizarPerfil = mock(ActualizarPerfilArtesanoUseCase.class);
        logos = mock(GestionarLogoArtesanoUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(
                new ArtesanoController(registrar, cambiarEstado, consultar, actualizarPerfil, logos)).build();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("el registro público deja la cuenta sin verificar y no devuelve la contraseña")
    void registro_publico() throws Exception {
        when(registrar.registrar("Ana Gómez", "ana@correo.com", "Segura1234", "1010", "300",
                "Cerámicas Ana", "Vasijas a mano", false)).thenReturn(artesano(1L, false));

        mvc.perform(post("/api/artesanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_REGISTRO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correoElectronico").value("ana@correo.com"))
                .andExpect(jsonPath("$.nombreEmprendimiento").value("Cerámicas Ana"))
                .andExpect(jsonPath("$.correoVerificado").value(false))
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andExpect(content().string(not(containsString("hash-secreto"))));
    }

    @Test
    @DisplayName("el registro por el administrador deja la cuenta ya verificada")
    void registro_por_el_administrador() throws Exception {
        when(registrar.registrar("Ana Gómez", "ana@correo.com", "Segura1234", "1010", "300",
                "Cerámicas Ana", "Vasijas a mano", true)).thenReturn(artesano(2L, true));

        mvc.perform(post("/api/artesanos/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_REGISTRO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correoVerificado").value(true));
    }

    @Test
    @DisplayName("GET /api/artesanos lista a los artesanos sin exponer contraseñas")
    void listar_todos() throws Exception {
        when(consultar.consultarTodos()).thenReturn(List.of(artesano(1L, true), artesano(2L, false)));

        mvc.perform(get("/api/artesanos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(content().string(not(containsString("hash-secreto"))));
    }

    @Test
    @DisplayName("GET /api/artesanos/perfil devuelve el perfil del artesano de la sesión")
    void mi_perfil() throws Exception {
        when(consultar.consultarPorId(7L)).thenReturn(artesano(7L, true));

        mvc.perform(get("/api/artesanos/perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.logo").value("logo.png"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/artesanos/perfil actualiza solo los datos permitidos")
    void actualizar_mi_perfil() throws Exception {
        when(actualizarPerfil.actualizar(7L, "Ana G.", "311", "Barro y Fuego", "Nueva descripción"))
                .thenReturn(artesano(7L, true));

        mvc.perform(put("/api/artesanos/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana G.\",\"telefono\":\"311\",\"nombreEmprendimiento\":\"Barro y Fuego\","
                                + "\"descripcionCorta\":\"Nueva descripción\"}"))
                .andExpect(status().isOk());

        verify(actualizarPerfil).actualizar(7L, "Ana G.", "311", "Barro y Fuego", "Nueva descripción");
    }

    @Test
    @DisplayName("PUT /api/artesanos/perfil/logo sube el logo del artesano de la sesión")
    void subir_el_logo() throws Exception {
        byte[] bytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2};
        when(logos.subirLogo(eq(7L), aryEq(bytes))).thenReturn(artesano(7L, true));

        mvc.perform(multipart(HttpMethod.PUT, "/api/artesanos/perfil/logo")
                        .file(new MockMultipartFile("archivo", "logo.jpg", "image/jpeg", bytes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logo").value("logo.png"));
    }

    @Test
    @DisplayName("DELETE /api/artesanos/perfil/logo quita el logo")
    void quitar_el_logo() throws Exception {
        when(logos.quitarLogo(7L)).thenReturn(artesano(7L, true));

        mvc.perform(delete("/api/artesanos/perfil/logo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @DisplayName("PUT /api/artesanos/{id}/estado activa o desactiva la cuenta")
    void cambiar_el_estado() throws Exception {
        mvc.perform(put("/api/artesanos/3/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk());

        verify(cambiarEstado).cambiarEstado(3L, false);
    }

    @Test
    @DisplayName("un correo o cédula repetidos responden 409")
    void dato_duplicado_responde_409() throws Exception {
        when(registrar.registrar("Ana Gómez", "ana@correo.com", "Segura1234", "1010", "300",
                "Cerámicas Ana", "Vasijas a mano", false))
                .thenThrow(new DatoDuplicadoException("Ese correo ya está registrado."));

        mvc.perform(post("/api/artesanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_REGISTRO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ese correo ya está registrado."));
    }

    @Test
    @DisplayName("un dato inválido responde 400 con el mensaje")
    void dato_invalido_responde_400() throws Exception {
        when(registrar.registrar("Ana Gómez", "ana@correo.com", "Segura1234", "1010", "300",
                "Cerámicas Ana", "Vasijas a mano", false))
                .thenThrow(new DatoInvalidoException("La contraseña es muy corta."));

        mvc.perform(post("/api/artesanos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_REGISTRO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La contraseña es muy corta."));
    }

    @Test
    @DisplayName("un artesano que no existe responde 404")
    void artesano_inexistente_responde_404() throws Exception {
        when(consultar.consultarPorId(7L)).thenThrow(new IllegalArgumentException("El artesano no existe."));

        mvc.perform(get("/api/artesanos/perfil"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El artesano no existe."));
    }
}