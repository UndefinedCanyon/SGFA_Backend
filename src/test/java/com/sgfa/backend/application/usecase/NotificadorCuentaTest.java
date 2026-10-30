package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.out.EnviadorCorreoPort;
import com.sgfa.backend.application.port.out.TokenCuentaRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.TipoTokenCuenta;
import com.sgfa.backend.domain.model.TokenCuenta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificadorCuentaTest {

    @Mock
    private TokenCuentaRepositoryPort tokenCuentaRepositoryPort;
    @Mock
    private EnviadorCorreoPort enviadorCorreoPort;

    private NotificadorCuenta notificador;

    @BeforeEach
    void preparar() {
        notificador = new NotificadorCuenta(tokenCuentaRepositoryPort, enviadorCorreoPort, "http://localhost:5173/");
    }

    private Artesano artesano(String nombre) {
        return new Artesano(7L, nombre, "maria@correo.com", "hash", "123456789", null,
                "Tejidos María", null, true, null, false);
    }

    private String extraerToken(String cuerpo, String ruta) {
        Matcher coincidencia = Pattern.compile(Pattern.quote(ruta) + "\\?token=([A-Za-z0-9_-]+)").matcher(cuerpo);
        assertTrue(coincidencia.find(), "El correo debe incluir el enlace con el token");
        return coincidencia.group(1);
    }

    @Test
    @DisplayName("La verificación guarda solo el hash del token y envía el enlace por correo")
    void envia_verificacion() {
        when(tokenCuentaRepositoryPort.buscarUltimaCreacion(7L, TipoTokenCuenta.VERIFICACION))
                .thenReturn(Optional.empty());

        boolean enviado = notificador.enviarVerificacion(artesano("María"));

        assertTrue(enviado);

        ArgumentCaptor<TokenCuenta> guardado = ArgumentCaptor.forClass(TokenCuenta.class);
        verify(tokenCuentaRepositoryPort).guardar(guardado.capture());
        TokenCuenta token = guardado.getValue();

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(enviadorCorreoPort).enviar(org.mockito.ArgumentMatchers.eq("maria@correo.com"),
                org.mockito.ArgumentMatchers.eq("Confirma tu correo en SGFA"), cuerpo.capture());

        String tokenPlano = extraerToken(cuerpo.getValue(), "http://localhost:5173/verificar-correo");

        assertNull(token.getId());
        assertEquals(7L, token.getIdArtesano());
        assertEquals(TipoTokenCuenta.VERIFICACION, token.getTipo());
        assertFalse(token.isUsado());
        assertEquals(TokenSeguro.hash(tokenPlano), token.getTokenHash());
        assertNotEquals(tokenPlano, token.getTokenHash());
        assertEquals(Duration.ofHours(24), Duration.between(token.getCreadoEn(), token.getExpiraEn()));
    }

    @Test
    @DisplayName("Antes de crear un enlace nuevo se anulan los anteriores sin usar")
    void invalida_los_anteriores() {
        when(tokenCuentaRepositoryPort.buscarUltimaCreacion(7L, TipoTokenCuenta.VERIFICACION))
                .thenReturn(Optional.empty());

        notificador.enviarVerificacion(artesano("María"));

        verify(tokenCuentaRepositoryPort).invalidarPendientes(7L, TipoTokenCuenta.VERIFICACION);
    }

    @Test
    @DisplayName("La recuperación dura 1 hora y apunta a la página de restablecer contraseña")
    void envia_recuperacion() {
        when(tokenCuentaRepositoryPort.buscarUltimaCreacion(7L, TipoTokenCuenta.RECUPERACION))
                .thenReturn(Optional.empty());

        boolean enviado = notificador.enviarRecuperacion(artesano("María"));

        assertTrue(enviado);

        ArgumentCaptor<TokenCuenta> guardado = ArgumentCaptor.forClass(TokenCuenta.class);
        verify(tokenCuentaRepositoryPort).guardar(guardado.capture());
        TokenCuenta token = guardado.getValue();

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(enviadorCorreoPort).enviar(org.mockito.ArgumentMatchers.eq("maria@correo.com"),
                org.mockito.ArgumentMatchers.eq("Restablece tu contraseña en SGFA"), cuerpo.capture());

        String tokenPlano = extraerToken(cuerpo.getValue(), "http://localhost:5173/restablecer-contrasena");

        assertEquals(TipoTokenCuenta.RECUPERACION, token.getTipo());
        assertEquals(TokenSeguro.hash(tokenPlano), token.getTokenHash());
        assertEquals(Duration.ofHours(1), Duration.between(token.getCreadoEn(), token.getExpiraEn()));
    }

    @Test
    @DisplayName("Si ya se envió uno hace menos de un minuto, no se envía otro")
    void espera_de_un_minuto() {
        when(tokenCuentaRepositoryPort.buscarUltimaCreacion(7L, TipoTokenCuenta.VERIFICACION))
                .thenReturn(Optional.of(LocalDateTime.now().minusSeconds(10)));

        boolean enviado = notificador.enviarVerificacion(artesano("María"));

        assertFalse(enviado);
        verify(tokenCuentaRepositoryPort, never()).invalidarPendientes(any(), any());
        verify(tokenCuentaRepositoryPort, never()).guardar(any());
        verify(enviadorCorreoPort, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Pasado el minuto de espera, el envío se permite de nuevo")
    void envia_despues_de_la_espera() {
        when(tokenCuentaRepositoryPort.buscarUltimaCreacion(7L, TipoTokenCuenta.VERIFICACION))
                .thenReturn(Optional.of(LocalDateTime.now().minusMinutes(2)));

        boolean enviado = notificador.enviarVerificacion(artesano("María"));

        assertTrue(enviado);
        verify(enviadorCorreoPort).enviar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("El nombre del artesano se escapa para que no inyecte HTML en el correo")
    void escapa_el_nombre() {
        when(tokenCuentaRepositoryPort.buscarUltimaCreacion(7L, TipoTokenCuenta.VERIFICACION))
                .thenReturn(Optional.empty());

        notificador.enviarVerificacion(artesano("<script>alert(1)</script>"));

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(enviadorCorreoPort).enviar(anyString(), anyString(), cuerpo.capture());
        assertFalse(cuerpo.getValue().contains("<script>"));
        assertTrue(cuerpo.getValue().contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
    }

    @Test
    @DisplayName("La dirección de la aplicación no duplica la barra final")
    void no_duplica_la_barra() {
        when(tokenCuentaRepositoryPort.buscarUltimaCreacion(7L, TipoTokenCuenta.VERIFICACION))
                .thenReturn(Optional.empty());

        notificador.enviarVerificacion(artesano("María"));

        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(enviadorCorreoPort).enviar(anyString(), anyString(), cuerpo.capture());
        assertTrue(cuerpo.getValue().contains("http://localhost:5173/verificar-correo?token="));
        assertFalse(cuerpo.getValue().contains("5173//"));
    }
}