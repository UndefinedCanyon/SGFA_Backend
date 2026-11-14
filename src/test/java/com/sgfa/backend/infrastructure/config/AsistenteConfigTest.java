package com.sgfa.backend.infrastructure.config;

import com.sgfa.backend.application.port.out.AsistenteIAPort;
import com.sgfa.backend.infrastructure.adapter.out.ia.AsistenteIAFalsoAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsistenteConfigTest {

    private static AsistenteIAPort crear(String proveedor, String clave) {
        return AsistenteConfig.crearAsistente(proveedor, clave, "modelo-x", 6, "https://gemini.test/v1beta");
    }

    @Test
    @DisplayName("«falso» siempre usa la demostración, aunque haya clave")
    void falso() {
        assertInstanceOf(AsistenteIAFalsoAdapter.class, crear("falso", "una-clave"));
        assertInstanceOf(AsistenteIAFalsoAdapter.class, crear("  FALSO ", ""));
    }

    @Test
    @DisplayName("«auto» usa Gemini si hay clave y la demostración si no")
    void auto() {
        assertEquals("Gemini", crear("auto", "una-clave").proveedor());
        assertInstanceOf(AsistenteIAFalsoAdapter.class, crear("auto", ""));
        assertInstanceOf(AsistenteIAFalsoAdapter.class, crear("auto", "   "));
        assertInstanceOf(AsistenteIAFalsoAdapter.class, crear("auto", null));
    }

    @Test
    @DisplayName("sin valor se comporta como «auto»")
    void sin_valor() {
        assertEquals("Gemini", crear(null, "una-clave").proveedor());
        assertInstanceOf(AsistenteIAFalsoAdapter.class, crear(null, ""));
    }

    @Test
    @DisplayName("«gemini» con clave usa Gemini")
    void gemini() {
        assertEquals("Gemini", crear("gemini", "una-clave").proveedor());
    }

    @Test
    @DisplayName("«gemini» sin clave falla al arrancar con un mensaje claro")
    void gemini_sin_clave() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> crear("gemini", ""));

        assertTrue(ex.getMessage().contains("GEMINI_API_KEY"));
    }

    @Test
    @DisplayName("un proveedor desconocido falla al arrancar")
    void proveedor_desconocido() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> crear("otro", "clave"));

        assertTrue(ex.getMessage().contains("auto, gemini o falso"));
    }
}
