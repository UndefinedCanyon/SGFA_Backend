package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.out.AdministradorRepositoryPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.CifradoContrasenaPort;
import com.sgfa.backend.domain.model.Administrador;
import com.sgfa.backend.domain.model.Artesano;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarArtesanoServiceTest {

    @Mock
    private ArtesanoRepositoryPort artesanoRepositoryPort;
    @Mock
    private AdministradorRepositoryPort administradorRepositoryPort;
    @Mock
    private CifradoContrasenaPort cifradoContrasenaPort;
    @Mock
    private NotificadorCuenta notificadorCuenta;

    private RegistrarArtesanoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new RegistrarArtesanoService(artesanoRepositoryPort, administradorRepositoryPort,
                cifradoContrasenaPort, notificadorCuenta);
    }

    private Artesano registrarValido(boolean correoVerificado) {
        return servicio.registrar("María Pérez", "maria@correo.com", "clave12345",
                "123456789", "3001234567", "Tejidos María", "Tejidos a mano", correoVerificado);
    }

    private void prepararGuardado() {
        when(cifradoContrasenaPort.cifrar("clave12345")).thenReturn("hash-de-prueba");
        when(artesanoRepositoryPort.guardar(any(Artesano.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("El registro público guarda al artesano cifrado y envía el correo de verificación")
    void registro_publico_envia_verificacion() {
        prepararGuardado();

        Artesano resultado = registrarValido(false);

        ArgumentCaptor<Artesano> captor = ArgumentCaptor.forClass(Artesano.class);
        verify(artesanoRepositoryPort).guardar(captor.capture());
        Artesano guardado = captor.getValue();

        assertEquals("hash-de-prueba", guardado.getContrasena());
        assertTrue(guardado.isActivo());
        assertFalse(guardado.isCorreoVerificado());
        assertNull(guardado.getId());
        verify(notificadorCuenta).enviarVerificacion(resultado);
    }

    @Test
    @DisplayName("El registro hecho por el administrador queda verificado y no envía correo")
    void registro_de_administrador_no_envia_verificacion() {
        prepararGuardado();

        Artesano resultado = registrarValido(true);

        assertTrue(resultado.isCorreoVerificado());
        verifyNoInteractions(notificadorCuenta);
    }

    @Test
    @DisplayName("El correo se guarda normalizado y los textos sin espacios sobrantes")
    void normaliza_correo_y_recorta_textos() {
        when(cifradoContrasenaPort.cifrar("clave12345")).thenReturn("hash-de-prueba");
        when(artesanoRepositoryPort.guardar(any(Artesano.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.registrar("  María Pérez  ", "  MARIA@Correo.COM ", "clave12345",
                " 123456789 ", null, "  Tejidos María  ", null, true);

        ArgumentCaptor<Artesano> captor = ArgumentCaptor.forClass(Artesano.class);
        verify(artesanoRepositoryPort).guardar(captor.capture());
        Artesano guardado = captor.getValue();

        assertEquals("maria@correo.com", guardado.getCorreoElectronico());
        assertEquals("María Pérez", guardado.getNombre());
        assertEquals("123456789", guardado.getCc());
        assertEquals("Tejidos María", guardado.getNombreEmprendimiento());
        assertNull(guardado.getTelefono());
        assertNull(guardado.getDescripcionCorta());
    }

    @Test
    @DisplayName("Un correo ya registrado como artesano se rechaza sin guardar")
    void correo_duplicado() {
        when(artesanoRepositoryPort.existePorCorreo("maria@correo.com")).thenReturn(true);

        assertThrows(DatoDuplicadoException.class, () -> registrarValido(false));

        verify(artesanoRepositoryPort, never()).guardar(any());
        verifyNoInteractions(notificadorCuenta);
    }

    @Test
    @DisplayName("Una cédula ya registrada se rechaza sin guardar")
    void cedula_duplicada() {
        when(artesanoRepositoryPort.existePorCc("123456789")).thenReturn(true);

        assertThrows(DatoDuplicadoException.class, () -> registrarValido(false));

        verify(artesanoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Un correo que ya usa un administrador no está disponible")
    void correo_de_administrador() {
        when(administradorRepositoryPort.buscarPorCorreo("maria@correo.com"))
                .thenReturn(Optional.of(new Administrador(1L, "Admin", "maria@correo.com", "hash")));

        assertThrows(DatoDuplicadoException.class, () -> registrarValido(false));

        verify(artesanoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Un correo con formato inválido se rechaza antes de consultar la base de datos")
    void correo_invalido() {
        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("María", "esto-no-es-un-correo",
                "clave12345", "123456789", null, "Tejidos", null, false));

        verifyNoInteractions(artesanoRepositoryPort);
    }

    @Test
    @DisplayName("Una contraseña de menos de 8 caracteres se rechaza")
    void contrasena_corta() {
        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("María", "maria@correo.com",
                "corta12", "123456789", null, "Tejidos", null, false));

        verifyNoInteractions(artesanoRepositoryPort);
    }

    @Test
    @DisplayName("Una contraseña de más de 72 bytes se rechaza")
    void contrasena_demasiado_larga() {
        String larga = "a".repeat(73);

        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("María", "maria@correo.com",
                larga, "123456789", null, "Tejidos", null, false));

        verifyNoInteractions(artesanoRepositoryPort);
    }

    @Test
    @DisplayName("El nombre, la cédula y el emprendimiento son obligatorios")
    void campos_obligatorios() {
        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("  ", "maria@correo.com",
                "clave12345", "123456789", null, "Tejidos", null, false));
        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("María", "maria@correo.com",
                "clave12345", "", null, "Tejidos", null, false));
        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("María", "maria@correo.com",
                "clave12345", "123456789", null, null, null, false));

        verifyNoInteractions(artesanoRepositoryPort);
    }

    @Test
    @DisplayName("Los textos que superan el máximo permitido se rechazan")
    void textos_demasiado_largos() {
        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("a".repeat(101), "maria@correo.com",
                "clave12345", "123456789", null, "Tejidos", null, false));
        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("María", "maria@correo.com",
                "clave12345", "1".repeat(21), null, "Tejidos", null, false));
        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("María", "maria@correo.com",
                "clave12345", "123456789", "3".repeat(21), "Tejidos", null, false));
        assertThrows(DatoInvalidoException.class, () -> servicio.registrar("María", "maria@correo.com",
                "clave12345", "123456789", null, "Tejidos", "d".repeat(301), false));

        verifyNoInteractions(artesanoRepositoryPort);
    }
}