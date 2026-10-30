package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.exception.TokenInvalidoException;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.CifradoContrasenaPort;
import com.sgfa.backend.application.port.out.TokenCuentaRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.TipoTokenCuenta;
import com.sgfa.backend.domain.model.TokenCuenta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentasServiceTest {

    private static final String TOKEN = "token-secreto-de-prueba";
    private static final String HASH = TokenSeguro.hash(TOKEN);

    @Mock
    private TokenCuentaRepositoryPort tokenCuentaRepositoryPort;
    @Mock
    private ArtesanoRepositoryPort artesanoRepositoryPort;
    @Mock
    private CifradoContrasenaPort cifradoContrasenaPort;
    @Mock
    private NotificadorCuenta notificadorCuenta;

    private Artesano artesano(boolean activo, boolean correoVerificado) {
        return new Artesano(7L, "María Pérez", "maria@correo.com", "hash-viejo", "123456789", null,
                "Tejidos María", null, activo, null, correoVerificado);
    }

    private TokenCuenta token(TipoTokenCuenta tipo, LocalDateTime expiraEn, boolean usado) {
        return new TokenCuenta(50L, 7L, tipo, HASH, LocalDateTime.now().minusMinutes(5), expiraEn, usado);
    }

    private TokenCuenta tokenVigente(TipoTokenCuenta tipo) {
        return token(tipo, LocalDateTime.now().plusHours(1), false);
    }

    @Nested
    @DisplayName("VerificarCorreoService")
    class Verificar {

        private VerificarCorreoService servicio() {
            return new VerificarCorreoService(tokenCuentaRepositoryPort, artesanoRepositoryPort);
        }

        @Test
        @DisplayName("Un enlace vigente confirma el correo del artesano")
        void verifica_correo() {
            Artesano sinVerificar = artesano(true, false);
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH))
                    .thenReturn(Optional.of(tokenVigente(TipoTokenCuenta.VERIFICACION)));
            when(tokenCuentaRepositoryPort.consumir(50L)).thenReturn(true);
            when(artesanoRepositoryPort.buscarPorId(7L)).thenReturn(Optional.of(sinVerificar));

            servicio().verificar(TOKEN);

            ArgumentCaptor<Artesano> captor = ArgumentCaptor.forClass(Artesano.class);
            verify(artesanoRepositoryPort).guardar(captor.capture());
            assertTrue(captor.getValue().isCorreoVerificado());
        }

        @Test
        @DisplayName("Los espacios alrededor del token se ignoran")
        void recorta_el_token() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH))
                    .thenReturn(Optional.of(tokenVigente(TipoTokenCuenta.VERIFICACION)));
            when(tokenCuentaRepositoryPort.consumir(50L)).thenReturn(true);
            when(artesanoRepositoryPort.buscarPorId(7L)).thenReturn(Optional.of(artesano(true, false)));

            servicio().verificar("  " + TOKEN + "  ");

            verify(artesanoRepositoryPort).guardar(any(Artesano.class));
        }

        @Test
        @DisplayName("Un token nulo o vacío se rechaza sin consultar nada")
        void token_vacio() {
            assertThrows(TokenInvalidoException.class, () -> servicio().verificar(null));
            assertThrows(TokenInvalidoException.class, () -> servicio().verificar("   "));

            verifyNoInteractions(tokenCuentaRepositoryPort, artesanoRepositoryPort);
        }

        @Test
        @DisplayName("Un token que no existe se rechaza")
        void token_inexistente() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH)).thenReturn(Optional.empty());

            assertThrows(TokenInvalidoException.class, () -> servicio().verificar(TOKEN));

            verify(artesanoRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Un enlace de recuperación no sirve para verificar el correo")
        void tipo_equivocado() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH))
                    .thenReturn(Optional.of(tokenVigente(TipoTokenCuenta.RECUPERACION)));

            assertThrows(TokenInvalidoException.class, () -> servicio().verificar(TOKEN));

            verify(tokenCuentaRepositoryPort, never()).consumir(any());
            verify(artesanoRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Un enlace vencido se rechaza")
        void token_vencido() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH)).thenReturn(Optional.of(
                    token(TipoTokenCuenta.VERIFICACION, LocalDateTime.now().minusMinutes(1), false)));

            assertThrows(TokenInvalidoException.class, () -> servicio().verificar(TOKEN));

            verify(tokenCuentaRepositoryPort, never()).consumir(any());
        }

        @Test
        @DisplayName("Un enlace ya usado se rechaza")
        void token_usado() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH)).thenReturn(Optional.of(
                    token(TipoTokenCuenta.VERIFICACION, LocalDateTime.now().plusHours(1), true)));

            assertThrows(TokenInvalidoException.class, () -> servicio().verificar(TOKEN));

            verify(tokenCuentaRepositoryPort, never()).consumir(any());
        }

        @Test
        @DisplayName("Si otro proceso gastó el enlace al mismo tiempo, no se verifica dos veces")
        void doble_uso_simultaneo() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH))
                    .thenReturn(Optional.of(tokenVigente(TipoTokenCuenta.VERIFICACION)));
            when(tokenCuentaRepositoryPort.consumir(50L)).thenReturn(false);

            assertThrows(TokenInvalidoException.class, () -> servicio().verificar(TOKEN));

            verify(artesanoRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Si el artesano ya no existe, el enlace se rechaza")
        void artesano_inexistente() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH))
                    .thenReturn(Optional.of(tokenVigente(TipoTokenCuenta.VERIFICACION)));
            when(tokenCuentaRepositoryPort.consumir(50L)).thenReturn(true);
            when(artesanoRepositoryPort.buscarPorId(7L)).thenReturn(Optional.empty());

            assertThrows(TokenInvalidoException.class, () -> servicio().verificar(TOKEN));
        }
    }

    @Nested
    @DisplayName("ReenviarVerificacionService")
    class Reenviar {

        private ReenviarVerificacionService servicio() {
            return new ReenviarVerificacionService(artesanoRepositoryPort, notificadorCuenta);
        }

        @Test
        @DisplayName("Reenvía el correo a un artesano activo con el correo sin confirmar")
        void reenvia() {
            Artesano pendiente = artesano(true, false);
            when(artesanoRepositoryPort.buscarPorCorreo("maria@correo.com")).thenReturn(Optional.of(pendiente));

            servicio().reenviar("  MARIA@Correo.com ");

            verify(notificadorCuenta).enviarVerificacion(pendiente);
        }

        @Test
        @DisplayName("Un correo vacío o nulo se rechaza")
        void correo_vacio() {
            assertThrows(DatoInvalidoException.class, () -> servicio().reenviar(null));
            assertThrows(DatoInvalidoException.class, () -> servicio().reenviar("  "));

            verifyNoInteractions(artesanoRepositoryPort, notificadorCuenta);
        }

        @Test
        @DisplayName("Si el correo ya estaba confirmado no se envía nada, y no se avisa")
        void ya_verificado() {
            when(artesanoRepositoryPort.buscarPorCorreo("maria@correo.com"))
                    .thenReturn(Optional.of(artesano(true, true)));

            servicio().reenviar("maria@correo.com");

            verifyNoInteractions(notificadorCuenta);
        }

        @Test
        @DisplayName("Una cuenta desactivada no recibe correo")
        void cuenta_desactivada() {
            when(artesanoRepositoryPort.buscarPorCorreo("maria@correo.com"))
                    .thenReturn(Optional.of(artesano(false, false)));

            servicio().reenviar("maria@correo.com");

            verifyNoInteractions(notificadorCuenta);
        }

        @Test
        @DisplayName("Un correo que no existe responde igual y no envía nada")
        void correo_inexistente() {
            when(artesanoRepositoryPort.buscarPorCorreo("nadie@correo.com")).thenReturn(Optional.empty());

            servicio().reenviar("nadie@correo.com");

            verifyNoInteractions(notificadorCuenta);
        }
    }

    @Nested
    @DisplayName("SolicitarRecuperacionService")
    class SolicitarRecuperacion {

        private SolicitarRecuperacionService servicio() {
            return new SolicitarRecuperacionService(artesanoRepositoryPort, notificadorCuenta);
        }

        @Test
        @DisplayName("Envía el enlace de recuperación a un artesano activo")
        void envia_recuperacion() {
            Artesano activo = artesano(true, true);
            when(artesanoRepositoryPort.buscarPorCorreo("maria@correo.com")).thenReturn(Optional.of(activo));

            servicio().solicitar(" Maria@Correo.com");

            verify(notificadorCuenta).enviarRecuperacion(activo);
        }

        @Test
        @DisplayName("También se envía aunque el correo no se hubiera confirmado")
        void envia_aunque_no_este_verificado() {
            Artesano sinVerificar = artesano(true, false);
            when(artesanoRepositoryPort.buscarPorCorreo("maria@correo.com")).thenReturn(Optional.of(sinVerificar));

            servicio().solicitar("maria@correo.com");

            verify(notificadorCuenta).enviarRecuperacion(sinVerificar);
        }

        @Test
        @DisplayName("Un correo vacío o nulo se rechaza")
        void correo_vacio() {
            assertThrows(DatoInvalidoException.class, () -> servicio().solicitar(null));
            assertThrows(DatoInvalidoException.class, () -> servicio().solicitar(""));

            verifyNoInteractions(artesanoRepositoryPort, notificadorCuenta);
        }

        @Test
        @DisplayName("Una cuenta desactivada no recibe el enlace")
        void cuenta_desactivada() {
            when(artesanoRepositoryPort.buscarPorCorreo("maria@correo.com"))
                    .thenReturn(Optional.of(artesano(false, true)));

            servicio().solicitar("maria@correo.com");

            verifyNoInteractions(notificadorCuenta);
        }

        @Test
        @DisplayName("Un correo que no existe responde igual y no envía nada")
        void correo_inexistente() {
            when(artesanoRepositoryPort.buscarPorCorreo("nadie@correo.com")).thenReturn(Optional.empty());

            servicio().solicitar("nadie@correo.com");

            verifyNoInteractions(notificadorCuenta);
        }
    }

    @Nested
    @DisplayName("RestablecerContrasenaService")
    class Restablecer {

        private RestablecerContrasenaService servicio() {
            return new RestablecerContrasenaService(tokenCuentaRepositoryPort, artesanoRepositoryPort,
                    cifradoContrasenaPort);
        }

        @Test
        @DisplayName("Cambia la contraseña (cifrada) y confirma el correo")
        void restablece() {
            Artesano sinVerificar = artesano(true, false);
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH))
                    .thenReturn(Optional.of(tokenVigente(TipoTokenCuenta.RECUPERACION)));
            when(tokenCuentaRepositoryPort.consumir(50L)).thenReturn(true);
            when(artesanoRepositoryPort.buscarPorId(7L)).thenReturn(Optional.of(sinVerificar));
            when(cifradoContrasenaPort.cifrar("nuevaClave123")).thenReturn("hash-nuevo");

            servicio().restablecer(TOKEN, "nuevaClave123");

            ArgumentCaptor<Artesano> captor = ArgumentCaptor.forClass(Artesano.class);
            verify(artesanoRepositoryPort).guardar(captor.capture());
            assertEquals("hash-nuevo", captor.getValue().getContrasena());
            assertTrue(captor.getValue().isCorreoVerificado());
        }

        @Test
        @DisplayName("Una contraseña débil se rechaza sin gastar el enlace")
        void contrasena_debil() {
            DatoInvalidoException error = assertThrows(DatoInvalidoException.class,
                    () -> servicio().restablecer(TOKEN, "corta"));

            assertEquals("La contraseña debe tener al menos 8 caracteres.", error.getMessage());
            verifyNoInteractions(tokenCuentaRepositoryPort, artesanoRepositoryPort, cifradoContrasenaPort);
        }

        @Test
        @DisplayName("Una contraseña de más de 72 bytes se rechaza")
        void contrasena_demasiado_larga() {
            assertThrows(DatoInvalidoException.class, () -> servicio().restablecer(TOKEN, "a".repeat(73)));

            verifyNoInteractions(tokenCuentaRepositoryPort);
        }

        @Test
        @DisplayName("Un token nulo o vacío se rechaza")
        void token_vacio() {
            assertThrows(TokenInvalidoException.class, () -> servicio().restablecer(null, "nuevaClave123"));
            assertThrows(TokenInvalidoException.class, () -> servicio().restablecer("  ", "nuevaClave123"));

            verifyNoInteractions(tokenCuentaRepositoryPort, artesanoRepositoryPort);
        }

        @Test
        @DisplayName("Un enlace de verificación no sirve para cambiar la contraseña")
        void tipo_equivocado() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH))
                    .thenReturn(Optional.of(tokenVigente(TipoTokenCuenta.VERIFICACION)));

            assertThrows(TokenInvalidoException.class, () -> servicio().restablecer(TOKEN, "nuevaClave123"));

            verify(tokenCuentaRepositoryPort, never()).consumir(any());
            verify(artesanoRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Un enlace vencido se rechaza")
        void token_vencido() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH)).thenReturn(Optional.of(
                    token(TipoTokenCuenta.RECUPERACION, LocalDateTime.now().minusSeconds(1), false)));

            assertThrows(TokenInvalidoException.class, () -> servicio().restablecer(TOKEN, "nuevaClave123"));

            verify(artesanoRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Un enlace ya usado se rechaza")
        void token_usado() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH)).thenReturn(Optional.of(
                    token(TipoTokenCuenta.RECUPERACION, LocalDateTime.now().plusHours(1), true)));

            assertThrows(TokenInvalidoException.class, () -> servicio().restablecer(TOKEN, "nuevaClave123"));

            verify(artesanoRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Un token que no existe se rechaza")
        void token_inexistente() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH)).thenReturn(Optional.empty());

            assertThrows(TokenInvalidoException.class, () -> servicio().restablecer(TOKEN, "nuevaClave123"));
        }

        @Test
        @DisplayName("Si otro proceso gastó el enlace al mismo tiempo, la contraseña no cambia")
        void doble_uso_simultaneo() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH))
                    .thenReturn(Optional.of(tokenVigente(TipoTokenCuenta.RECUPERACION)));
            when(tokenCuentaRepositoryPort.consumir(50L)).thenReturn(false);

            assertThrows(TokenInvalidoException.class, () -> servicio().restablecer(TOKEN, "nuevaClave123"));

            verify(artesanoRepositoryPort, never()).guardar(any());
            verify(cifradoContrasenaPort, never()).cifrar(any());
        }

        @Test
        @DisplayName("Si el artesano ya no existe, el enlace se rechaza")
        void artesano_inexistente() {
            when(tokenCuentaRepositoryPort.buscarPorHash(HASH))
                    .thenReturn(Optional.of(tokenVigente(TipoTokenCuenta.RECUPERACION)));
            when(tokenCuentaRepositoryPort.consumir(50L)).thenReturn(true);
            when(artesanoRepositoryPort.buscarPorId(7L)).thenReturn(Optional.empty());

            assertThrows(TokenInvalidoException.class, () -> servicio().restablecer(TOKEN, "nuevaClave123"));
        }
    }
}