package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.CorreoNoVerificadoException;
import com.sgfa.backend.application.exception.CredencialesInvalidasException;
import com.sgfa.backend.application.port.in.IniciarSesionUseCase;
import com.sgfa.backend.domain.model.SesionIniciada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private IniciarSesionUseCase iniciarSesion;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        iniciarSesion = mock(IniciarSesionUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(iniciarSesion))
                .setControllerAdvice(new ManejadorGlobalErrores())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login devuelve la sesión iniciada")
    void login_devuelve_la_sesion() throws Exception {
        when(iniciarSesion.iniciarSesion("ana@correo.com", "Segura1234"))
                .thenReturn(new SesionIniciada(7L, "Ana", "ana@correo.com", "ARTESANO", "token-123"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoElectronico\":\"ana@correo.com\",\"contrasena\":\"Segura1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.correoElectronico").value("ana@correo.com"))
                .andExpect(jsonPath("$.rol").value("ARTESANO"))
                .andExpect(jsonPath("$.token").value("token-123"));

        verify(iniciarSesion).iniciarSesion("ana@correo.com", "Segura1234");
    }

    @Test
    @DisplayName("credenciales incorrectas responden 401 con el mensaje")
    void credenciales_incorrectas_responden_401() throws Exception {
        when(iniciarSesion.iniciarSesion("ana@correo.com", "mala"))
                .thenThrow(new CredencialesInvalidasException("Credenciales incorrectas"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoElectronico\":\"ana@correo.com\",\"contrasena\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }

    @Test
    @DisplayName("un correo sin verificar responde 403 con su código")
    void correo_sin_verificar_responde_403() throws Exception {
        when(iniciarSesion.iniciarSesion("ana@correo.com", "Segura1234"))
                .thenThrow(new CorreoNoVerificadoException("Confirma tu correo"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correoElectronico\":\"ana@correo.com\",\"contrasena\":\"Segura1234\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CORREO_NO_VERIFICADO"));
    }
}