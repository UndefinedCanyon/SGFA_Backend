package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.CorreoNoVerificadoException;
import com.sgfa.backend.application.exception.CredencialesInvalidasException;
import com.sgfa.backend.application.port.out.AdministradorRepositoryPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.CifradoContrasenaPort;
import com.sgfa.backend.application.port.out.GeneradorTokenPort;
import com.sgfa.backend.domain.model.Administrador;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.SesionIniciada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IniciarSesionServiceTest {

    private static final String CORREO = "maria@correo.com";
    private static final String CLAVE_PLANA = "clave123";
    private static final String CLAVE_CIFRADA = "$2a$10$hashDePrueba";

    @Mock
    private ArtesanoRepositoryPort artesanoRepositoryPort;
    @Mock
    private AdministradorRepositoryPort administradorRepositoryPort;
    @Mock
    private CifradoContrasenaPort cifradoContrasenaPort;
    @Mock
    private GeneradorTokenPort generadorTokenPort;

    private IniciarSesionService servicio;

    @BeforeEach
    void preparar() {
        servicio = new IniciarSesionService(artesanoRepositoryPort, administradorRepositoryPort,
                cifradoContrasenaPort, generadorTokenPort);
    }

    private Artesano artesano(boolean activo, boolean correoVerificado) {
        return new Artesano(7L, "María Pérez", CORREO, CLAVE_CIFRADA, "123456789", "3001234567",
                "Tejidos María", "Tejidos a mano", activo, null, correoVerificado);
    }

    private Administrador administrador() {
        return new Administrador(1L, "Admin", "admin@sgfa.com", CLAVE_CIFRADA);
    }

    @Test
    @DisplayName("Un artesano activo y verificado inicia sesión y recibe su token")
    void artesano_valido_inicia_sesion() {
        when(artesanoRepositoryPort.buscarPorCorreo(CORREO)).thenReturn(Optional.of(artesano(true, true)));
        when(cifradoContrasenaPort.coincide(CLAVE_PLANA, CLAVE_CIFRADA)).thenReturn(true);
        when(generadorTokenPort.generar(CORREO, "ARTESANO", 7L)).thenReturn("token-artesano");

        SesionIniciada sesion = servicio.iniciarSesion(CORREO, CLAVE_PLANA);

        assertEquals(7L, sesion.getId());
        assertEquals("María Pérez", sesion.getNombre());
        assertEquals(CORREO, sesion.getCorreoElectronico());
        assertEquals("ARTESANO", sesion.getRol());
        assertEquals("token-artesano", sesion.getToken());
        verifyNoInteractions(administradorRepositoryPort);
    }

    @Test
    @DisplayName("El correo se normaliza (espacios y mayúsculas) antes de buscarlo")
    void normaliza_el_correo() {
        when(artesanoRepositoryPort.buscarPorCorreo(CORREO)).thenReturn(Optional.of(artesano(true, true)));
        when(cifradoContrasenaPort.coincide(CLAVE_PLANA, CLAVE_CIFRADA)).thenReturn(true);
        when(generadorTokenPort.generar(CORREO, "ARTESANO", 7L)).thenReturn("token");

        SesionIniciada sesion = servicio.iniciarSesion("  MARIA@Correo.COM  ", CLAVE_PLANA);

        assertEquals("token", sesion.getToken());
        verify(artesanoRepositoryPort).buscarPorCorreo(CORREO);
    }

    @Test
    @DisplayName("Un artesano con contraseña incorrecta recibe credenciales inválidas")
    void artesano_con_contrasena_incorrecta() {
        when(artesanoRepositoryPort.buscarPorCorreo(CORREO)).thenReturn(Optional.of(artesano(true, true)));
        when(cifradoContrasenaPort.coincide("otraClave", CLAVE_CIFRADA)).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion(CORREO, "otraClave"));

        verify(generadorTokenPort, never()).generar(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Un artesano desactivado no puede entrar y no se revela que existe")
    void artesano_desactivado() {
        when(artesanoRepositoryPort.buscarPorCorreo(CORREO)).thenReturn(Optional.of(artesano(false, true)));

        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion(CORREO, CLAVE_PLANA));

        verify(generadorTokenPort, never()).generar(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Un artesano con correo sin verificar recibe el aviso de verificación")
    void artesano_sin_verificar_correo() {
        when(artesanoRepositoryPort.buscarPorCorreo(CORREO)).thenReturn(Optional.of(artesano(true, false)));
        when(cifradoContrasenaPort.coincide(CLAVE_PLANA, CLAVE_CIFRADA)).thenReturn(true);

        assertThrows(CorreoNoVerificadoException.class, () -> servicio.iniciarSesion(CORREO, CLAVE_PLANA));

        verify(generadorTokenPort, never()).generar(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Con contraseña incorrecta no se revela si el correo está sin verificar")
    void contrasena_incorrecta_tiene_prioridad_sobre_correo_sin_verificar() {
        when(artesanoRepositoryPort.buscarPorCorreo(CORREO)).thenReturn(Optional.of(artesano(true, false)));
        when(cifradoContrasenaPort.coincide("otraClave", CLAVE_CIFRADA)).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion(CORREO, "otraClave"));
    }

    @Test
    @DisplayName("Un administrador válido inicia sesión con rol ADMINISTRADOR")
    void administrador_valido_inicia_sesion() {
        when(artesanoRepositoryPort.buscarPorCorreo("admin@sgfa.com")).thenReturn(Optional.empty());
        when(administradorRepositoryPort.buscarPorCorreo("admin@sgfa.com")).thenReturn(Optional.of(administrador()));
        when(cifradoContrasenaPort.coincide("admin123", CLAVE_CIFRADA)).thenReturn(true);
        when(generadorTokenPort.generar("admin@sgfa.com", "ADMINISTRADOR", 1L)).thenReturn("token-admin");

        SesionIniciada sesion = servicio.iniciarSesion("admin@sgfa.com", "admin123");

        assertEquals(1L, sesion.getId());
        assertEquals("ADMINISTRADOR", sesion.getRol());
        assertEquals("token-admin", sesion.getToken());
    }

    @Test
    @DisplayName("Un administrador con contraseña incorrecta recibe credenciales inválidas")
    void administrador_con_contrasena_incorrecta() {
        when(artesanoRepositoryPort.buscarPorCorreo("admin@sgfa.com")).thenReturn(Optional.empty());
        when(administradorRepositoryPort.buscarPorCorreo("admin@sgfa.com")).thenReturn(Optional.of(administrador()));
        when(cifradoContrasenaPort.coincide("mala", CLAVE_CIFRADA)).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion("admin@sgfa.com", "mala"));

        verify(generadorTokenPort, never()).generar(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Un correo que no existe recibe el mismo mensaje que una contraseña mala")
    void correo_inexistente() {
        when(artesanoRepositoryPort.buscarPorCorreo("nadie@correo.com")).thenReturn(Optional.empty());
        when(administradorRepositoryPort.buscarPorCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        CredencialesInvalidasException error = assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion("nadie@correo.com", CLAVE_PLANA));

        assertEquals("Correo o contraseña incorrectos.", error.getMessage());
        verify(cifradoContrasenaPort, never()).coincide(any(), any());
    }

    @Test
    @DisplayName("Un correo nulo se trata como credenciales inválidas, sin errores inesperados")
    void correo_nulo() {
        when(artesanoRepositoryPort.buscarPorCorreo("")).thenReturn(Optional.empty());
        when(administradorRepositoryPort.buscarPorCorreo("")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> servicio.iniciarSesion(null, CLAVE_PLANA));
    }
}