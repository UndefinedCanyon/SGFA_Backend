package com.sgfa.backend.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdaptadoresSeguridadTest {

    @Test
    @DisplayName("el generador de tokens delega en JwtService")
    void el_generador_delega_en_jwt_service() {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.generarToken("ana@correo.com", "ARTESANO", 3L)).thenReturn("token-firmado");
        JwtGeneradorTokenAdapter adaptador = new JwtGeneradorTokenAdapter(jwtService);

        String token = adaptador.generar("ana@correo.com", "ARTESANO", 3L);

        assertEquals("token-firmado", token);
        verify(jwtService).generarToken("ana@correo.com", "ARTESANO", 3L);
    }

    @Test
    @DisplayName("la contraseña cifrada no es la contraseña original")
    void la_contrasena_cifrada_no_es_la_original() {
        BCryptCifradoAdapter adaptador = new BCryptCifradoAdapter(new BCryptPasswordEncoder());

        String cifrada = adaptador.cifrar("Segura1234");

        assertNotEquals("Segura1234", cifrada);
        assertTrue(cifrada.startsWith("$2"), "debería ser un hash BCrypt");
    }

    @Test
    @DisplayName("la contraseña correcta coincide con su hash y una incorrecta no")
    void coincide_solo_con_la_contrasena_correcta() {
        BCryptCifradoAdapter adaptador = new BCryptCifradoAdapter(new BCryptPasswordEncoder());
        String cifrada = adaptador.cifrar("Segura1234");

        assertTrue(adaptador.coincide("Segura1234", cifrada));
        assertFalse(adaptador.coincide("Segura9999", cifrada));
    }

    @Test
    @DisplayName("dos cifrados de la misma contraseña son distintos, pero ambos coinciden")
    void cada_cifrado_usa_una_sal_distinta() {
        BCryptCifradoAdapter adaptador = new BCryptCifradoAdapter(new BCryptPasswordEncoder());

        String primero = adaptador.cifrar("Segura1234");
        String segundo = adaptador.cifrar("Segura1234");

        assertNotEquals(primero, segundo);
        assertTrue(adaptador.coincide("Segura1234", primero));
        assertTrue(adaptador.coincide("Segura1234", segundo));
    }
}