package com.sgfa.backend.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String CLAVE = "clave-de-prueba-con-mas-de-treinta-y-dos-bytes-0123456789";
    private static final long UNA_HORA_MS = 3_600_000L;

    private static JwtService servicio(String clave, long duracionMs) {
        JwtService servicio = new JwtService();
        ReflectionTestUtils.setField(servicio, "claveSecreta", clave);
        ReflectionTestUtils.setField(servicio, "duracionMs", duracionMs);
        return servicio;
    }

    @Test
    @DisplayName("el token generado lleva el correo, el rol y el id del usuario")
    void el_token_lleva_los_datos_del_usuario() {
        JwtService jwt = servicio(CLAVE, UNA_HORA_MS);

        String token = jwt.generarToken("ana@correo.com", "ARTESANO", 7L);

        assertNotNull(token);
        assertEquals("ana@correo.com", jwt.extraerCorreo(token));
        assertEquals("ARTESANO", jwt.extraerRol(token));
        assertEquals(7L, jwt.extraerId(token));
    }

    @Test
    @DisplayName("el token vence después del tiempo configurado")
    void el_token_vence_segun_la_duracion_configurada() {
        JwtService jwt = servicio(CLAVE, UNA_HORA_MS);

        Claims claims = jwt.extraerClaims(jwt.generarToken("ana@correo.com", "ARTESANO", 7L));

        long duracion = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertTrue(duracion >= UNA_HORA_MS - 1000 && duracion <= UNA_HORA_MS + 1000,
                "la duración real fue " + duracion + " ms");
    }

    @Test
    @DisplayName("un token recién generado es válido")
    void un_token_reciente_es_valido() {
        JwtService jwt = servicio(CLAVE, UNA_HORA_MS);

        assertTrue(jwt.esTokenValido(jwt.generarToken("ana@correo.com", "ADMINISTRADOR", 1L)));
    }

    @Test
    @DisplayName("un token vencido no es válido y no se puede leer")
    void un_token_vencido_no_es_valido() {
        JwtService jwt = servicio(CLAVE, -60_000L);
        String token = jwt.generarToken("ana@correo.com", "ARTESANO", 7L);

        assertFalse(jwt.esTokenValido(token));
        assertThrows(JwtException.class, () -> jwt.extraerClaims(token));
    }

    @Test
    @DisplayName("un token firmado con otra clave se rechaza")
    void un_token_firmado_con_otra_clave_se_rechaza() {
        JwtService ajeno = servicio("otra-clave-distinta-con-mas-de-treinta-y-dos-bytes-9876543210", UNA_HORA_MS);
        String tokenAjeno = ajeno.generarToken("intruso@correo.com", "ADMINISTRADOR", 99L);

        JwtService jwt = servicio(CLAVE, UNA_HORA_MS);

        assertFalse(jwt.esTokenValido(tokenAjeno));
        assertThrows(JwtException.class, () -> jwt.extraerClaims(tokenAjeno));
    }

    @Test
    @DisplayName("un token alterado se rechaza")
    void un_token_alterado_se_rechaza() {
        JwtService jwt = servicio(CLAVE, UNA_HORA_MS);
        String token = jwt.generarToken("ana@correo.com", "ARTESANO", 7L);

        String alterado = token.substring(0, token.length() - 4) + "AAAA";

        assertFalse(jwt.esTokenValido(alterado));
    }

    @Test
    @DisplayName("texto que no es un token, vacío o nulo no es válido")
    void texto_que_no_es_un_token_no_es_valido() {
        JwtService jwt = servicio(CLAVE, UNA_HORA_MS);

        assertFalse(jwt.esTokenValido("esto-no-es-un-jwt"));
        assertFalse(jwt.esTokenValido(""));
        assertFalse(jwt.esTokenValido(null));
    }
}