package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.out.EnviadorCorreoPort;
import com.sgfa.backend.application.port.out.TokenCuentaRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.TipoTokenCuenta;
import com.sgfa.backend.domain.model.TokenCuenta;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class NotificadorCuenta {

    private static final Duration ESPERA_ENTRE_ENVIOS = Duration.ofSeconds(60);
    private static final Duration VIGENCIA_VERIFICACION = Duration.ofHours(24);
    private static final Duration VIGENCIA_RECUPERACION = Duration.ofHours(1);

    private final TokenCuentaRepositoryPort tokenCuentaRepositoryPort;
    private final EnviadorCorreoPort enviadorCorreoPort;
    private final String urlApp;

    public NotificadorCuenta(TokenCuentaRepositoryPort tokenCuentaRepositoryPort,
                              EnviadorCorreoPort enviadorCorreoPort,
                              String urlApp) {
        this.tokenCuentaRepositoryPort = tokenCuentaRepositoryPort;
        this.enviadorCorreoPort = enviadorCorreoPort;
        this.urlApp = urlApp.endsWith("/") ? urlApp.substring(0, urlApp.length() - 1) : urlApp;
    }

    /** Devuelve true si se envió el correo; false si se omitió por la espera de un minuto. */
    public boolean enviarVerificacion(Artesano artesano) {
        return emitir(artesano, TipoTokenCuenta.VERIFICACION, VIGENCIA_VERIFICACION,
                "/verificar-correo",
                "Confirma tu correo en SGFA",
                "Confirma tu correo electrónico",
                "Gracias por registrarte en SGFA. Para activar tu cuenta, confirma que este correo es tuyo.",
                "Confirmar mi correo",
                "El enlace vence en 24 horas y solo se puede usar una vez. Si no creaste esta cuenta, ignora este mensaje.");
    }

    public boolean enviarRecuperacion(Artesano artesano) {
        return emitir(artesano, TipoTokenCuenta.RECUPERACION, VIGENCIA_RECUPERACION,
                "/restablecer-contrasena",
                "Restablece tu contraseña en SGFA",
                "Restablece tu contraseña",
                "Recibimos una solicitud para cambiar la contraseña de tu cuenta. Pulsa el botón para elegir una nueva.",
                "Cambiar mi contraseña",
                "El enlace vence en 1 hora y solo se puede usar una vez. Si no lo pediste, ignora este mensaje: tu contraseña no cambia.");
    }

    private boolean emitir(Artesano artesano, TipoTokenCuenta tipo, Duration vigencia, String ruta,
                           String asunto, String titulo, String texto, String textoBoton, String nota) {
        LocalDateTime ahora = LocalDateTime.now();

        Optional<LocalDateTime> ultimo = tokenCuentaRepositoryPort.buscarUltimaCreacion(artesano.getId(), tipo);
        if (ultimo.isPresent() && ultimo.get().plus(ESPERA_ENTRE_ENVIOS).isAfter(ahora)) {
            return false;
        }

        tokenCuentaRepositoryPort.invalidarPendientes(artesano.getId(), tipo);

        String token = TokenSeguro.generar();
        tokenCuentaRepositoryPort.guardar(new TokenCuenta(
                null, artesano.getId(), tipo, TokenSeguro.hash(token), ahora, ahora.plus(vigencia), false));

        String enlace = urlApp + ruta + "?token=" + token;
        String cuerpo = PlantillaCorreo.construir(
                artesano.getNombre(), titulo, texto, textoBoton, enlace, nota);

        enviadorCorreoPort.enviar(artesano.getCorreoElectronico(), asunto, cuerpo);
        return true;
    }
}