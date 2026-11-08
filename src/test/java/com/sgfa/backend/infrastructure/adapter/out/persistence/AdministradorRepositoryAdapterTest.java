package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.Administrador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdministradorRepositoryAdapterTest extends PersistenciaTestBase {

    @Autowired
    private AdministradorRepositoryAdapter administradores;

    @Test
    @DisplayName("encuentra al administrador por su correo")
    void buscar_por_correo() {
        Administrador admin = administradores.buscarPorCorreo("admin@sgfa.com").orElseThrow();

        assertEquals("Admin SGFA", admin.getNombre());
        assertEquals("hash-admin", admin.getContrasena());
        assertEquals("admin@sgfa.com", admin.getCorreoElectronico());
    }

    @Test
    @DisplayName("devuelve vacío si el correo no pertenece a ningún administrador")
    void correo_inexistente() {
        assertTrue(administradores.buscarPorCorreo("otro@sgfa.com").isEmpty());
    }
}