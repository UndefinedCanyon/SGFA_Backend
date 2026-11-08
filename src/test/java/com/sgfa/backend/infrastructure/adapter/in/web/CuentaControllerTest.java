package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.TokenInvalidoException;
import com.sgfa.backend.application.port.in.ReenviarVerificacionUseCase;
import com.sgfa.backend.application.port.in.RestablecerContrasenaUseCase;
import com.sgfa.backend.application.port.in.SolicitarRecuperacionUseCase;
import com.sgfa.backend.application.port.in.VerificarCorreoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CuentaControllerTest {

    private VerificarCorreoUseCase verificarCorreo;
    private ReenviarVerificacionUseCase reenviarVerificacion;
    private SolicitarRecuperacionUseCase solicitarRecuperacion;
    private RestablecerContrasenaUseCase restablecerContrasena;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        verificarCorreo = mock(VerificarCorreoUseCase.class);
        reenviarVerificacion = mock(ReenviarVerificacionUseCase.class);
        solicitarRecuperacion = mock(SolicitarRecuperacionUseCase.class);
        restablecerContrasena = mock(RestablecerContrasenaUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new CuentaController(
                        verificarCorreo, reenviarVerificacion, solicitarRecuperacion, restablecerContrasena))
                .setControllerAdvice(new ManejadorGlobalErrores())
                .build();
    }

    @Test
    @DisplayName("verificar-correo confirma el token recibido")
    void verificar_correo_confirma_el_token() throws Exception {
        mvc.perform(post("/api/auth/verificar-correo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());

        verify(verificarCorreo).verificar("abc123");
    }

    @Test
    @DisplayName("verificar-correo con un token inválido responde 400 con su código")
    void verificar_correo_con_token_invalido_responde_400() throws Exception {
        doThrow(new TokenInvalidoException("El enlace venció")).when(verificarCorreo).verificar("viejo");

        mvc.perform(post("/api/auth/verificar-correo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"viejo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }

    @Test
    @DisplayName("reenviar-verificacion pide el reenvío y da una respuesta genérica")
    void reenviar_verificacion_da_una_respuesta_generica() throws Exception {
        mvc.perform(post("/api/auth/reenviar-verificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoElectronico\":\"ana@correo.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());

        verify(reenviarVerificacion).reenviar("ana@correo.com");
    }

    @Test
    @DisplayName("olvide-contrasena pide la recuperación y da una respuesta genérica")
    void olvide_contrasena_da_una_respuesta_generica() throws Exception {
        mvc.perform(post("/api/auth/olvide-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoElectronico\":\"ana@correo.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());

        verify(solicitarRecuperacion).solicitar("ana@correo.com");
    }

    @Test
    @DisplayName("la respuesta es la misma tanto si piden reenviar como si piden recuperar")
    void las_respuestas_genericas_no_revelan_si_el_correo_existe() throws Exception {
        String reenviar = mvc.perform(post("/api/auth/reenviar-verificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoElectronico\":\"a@b.com\"}"))
                .andReturn().getResponse().getContentAsString();
        String recuperar = mvc.perform(post("/api/auth/olvide-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoElectronico\":\"a@b.com\"}"))
                .andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertEquals(reenviar, recuperar);
    }

    @Test
    @DisplayName("restablecer-contrasena cambia la contraseña con el token recibido")
    void restablecer_contrasena_cambia_la_contrasena() throws Exception {
        mvc.perform(post("/api/auth/restablecer-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc123\",\"contrasena\":\"Nueva12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());

        verify(restablecerContrasena).restablecer("abc123", "Nueva12345");
    }

    @Test
    @DisplayName("restablecer-contrasena con un token vencido responde 400")
    void restablecer_contrasena_con_token_vencido_responde_400() throws Exception {
        doThrow(new TokenInvalidoException("El enlace venció"))
                .when(restablecerContrasena).restablecer("viejo", "Nueva12345");

        mvc.perform(post("/api/auth/restablecer-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"viejo\",\"contrasena\":\"Nueva12345\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El enlace venció"));
    }
}