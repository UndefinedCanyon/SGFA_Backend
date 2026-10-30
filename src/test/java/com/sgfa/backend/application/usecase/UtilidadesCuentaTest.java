package com.sgfa.backend.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilidadesCuentaTest {

    @Nested
    @DisplayName("TokenSeguro")
    class Tokens {

        @Test
        @DisplayName("Genera tokens de 43 caracteres seguros para una URL")
        void formato_del_token() {
            String token = TokenSeguro.generar();

            assertEquals(43, token.length());
            assertTrue(token.matches("[A-Za-z0-9_-]+"));
        }

        @Test
        @DisplayName("Cada token generado es distinto")
        void tokens_distintos() {
            Set<String> generados = new HashSet<>();
            for (int i = 0; i < 200; i++) {
                generados.add(TokenSeguro.generar());
            }

            assertEquals(200, generados.size());
        }

        @Test
        @DisplayName("El hash es SHA-256 en hexadecimal (valor conocido de «abc»)")
        void hash_conocido() {
            assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                    TokenSeguro.hash("abc"));
        }

        @Test
        @DisplayName("El hash es siempre el mismo para el mismo token y distinto para otro")
        void hash_determinista() {
            assertEquals(TokenSeguro.hash("token-uno"), TokenSeguro.hash("token-uno"));
            assertNotEquals(TokenSeguro.hash("token-uno"), TokenSeguro.hash("token-dos"));
            assertEquals(64, TokenSeguro.hash("token-uno").length());
        }
    }

    @Nested
    @DisplayName("PlantillaCorreo")
    class Plantilla {

        @Test
        @DisplayName("Escapa los caracteres que permitirían inyectar HTML")
        void escapa_html() {
            assertEquals("&lt;a href=&quot;x&quot;&gt;&amp;&#39;&lt;/a&gt;",
                    PlantillaCorreo.escapar("<a href=\"x\">&'</a>"));
        }

        @Test
        @DisplayName("Un texto nulo se convierte en vacío")
        void texto_nulo() {
            assertEquals("", PlantillaCorreo.escapar(null));
        }

        @Test
        @DisplayName("El correo incluye el título, el saludo, el botón y el enlace dos veces")
        void construye_el_correo() {
            String html = PlantillaCorreo.construir("María", "Confirma tu correo", "Texto del mensaje",
                    "Confirmar", "http://localhost:5173/verificar-correo?token=abc", "Nota final");

            assertTrue(html.contains("<h1 style=\"margin:0 0 16px;font-size:20px;color:#111827;\">Confirma tu correo</h1>"));
            assertTrue(html.contains("Hola, María."));
            assertTrue(html.contains("Texto del mensaje"));
            assertTrue(html.contains("Nota final"));
            int apariciones = html.split("verificar-correo\\?token=abc", -1).length - 1;
            assertEquals(2, apariciones);
        }

        @Test
        @DisplayName("Los datos del usuario no pasan sin escapar al correo")
        void escapa_los_datos() {
            String html = PlantillaCorreo.construir("<b>Ana</b>", "Título", "Texto", "Botón", "http://x", "Nota");

            assertFalse(html.contains("<b>Ana</b>"));
            assertTrue(html.contains("&lt;b&gt;Ana&lt;/b&gt;"));
        }
    }
}