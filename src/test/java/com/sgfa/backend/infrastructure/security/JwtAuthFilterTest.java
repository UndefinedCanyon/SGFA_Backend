package com.sgfa.backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthFilterTest {

    private static final String CLAVE = "clave-de-prueba-con-mas-de-treinta-y-dos-bytes-0123456789";

    private JwtService jwt;
    private JwtAuthFilter filtro;
    private MockHttpServletRequest peticion;
    private MockHttpServletResponse respuesta;
    private FilterChain cadena;

    private static JwtService servicio(long duracionMs) {
        JwtService servicio = new JwtService();
        ReflectionTestUtils.setField(servicio, "claveSecreta", CLAVE);
        ReflectionTestUtils.setField(servicio, "duracionMs", duracionMs);
        return servicio;
    }

    @BeforeEach
    void preparar() {
        SecurityContextHolder.clearContext();
        jwt = servicio(3_600_000L);
        filtro = new JwtAuthFilter(jwt);
        peticion = new MockHttpServletRequest();
        respuesta = new MockHttpServletResponse();
        cadena = mock(FilterChain.class);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("sin encabezado Authorization deja pasar la petición sin sesión")
    void sin_encabezado_no_autentica() throws Exception {
        filtro.doFilter(peticion, respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    @DisplayName("un encabezado que no es Bearer no autentica")
    void encabezado_que_no_es_bearer_no_autentica() throws Exception {
        peticion.addHeader("Authorization", "Basic dXN1YXJpbzpjbGF2ZQ==");

        filtro.doFilter(peticion, respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    @DisplayName("un token válido deja al usuario autenticado con su rol")
    void token_valido_autentica_al_usuario() throws Exception {
        String token = jwt.generarToken("ana@correo.com", "ARTESANO", 7L);
        peticion.addHeader("Authorization", "Bearer " + token);

        filtro.doFilter(peticion, respuesta, cadena);

        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(autenticacion);
        UsuarioAutenticado usuario = assertInstanceOf(UsuarioAutenticado.class, autenticacion.getPrincipal());
        assertEquals(7L, usuario.getId());
        assertEquals("ana@correo.com", usuario.getCorreoElectronico());
        assertEquals("ARTESANO", usuario.getRol());
        assertTrue(autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ARTESANO")));
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    @DisplayName("un token inválido no autentica, pero la petición sigue su curso")
    void token_invalido_no_autentica() throws Exception {
        peticion.addHeader("Authorization", "Bearer esto-no-es-un-token");

        filtro.doFilter(peticion, respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(cadena).doFilter(peticion, respuesta);
    }

    @Test
    @DisplayName("un token vencido no autentica")
    void token_vencido_no_autentica() throws Exception {
        String vencido = servicio(-60_000L).generarToken("ana@correo.com", "ARTESANO", 7L);
        peticion.addHeader("Authorization", "Bearer " + vencido);

        filtro.doFilter(peticion, respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(cadena).doFilter(peticion, respuesta);
    }
}