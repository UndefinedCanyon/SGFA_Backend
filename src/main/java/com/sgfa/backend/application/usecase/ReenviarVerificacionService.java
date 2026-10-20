package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ReenviarVerificacionUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;

public class ReenviarVerificacionService implements ReenviarVerificacionUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final NotificadorCuenta notificadorCuenta;

    public ReenviarVerificacionService(ArtesanoRepositoryPort artesanoRepositoryPort,
                                        NotificadorCuenta notificadorCuenta) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.notificadorCuenta = notificadorCuenta;
    }

    @Override
    public void reenviar(String correoElectronico) {
        String correo = DatosCuenta.normalizarCorreo(correoElectronico);
        if (correo.isEmpty()) {
            throw new DatoInvalidoException("Indica tu correo electrónico.");
        }

        // Si el correo no existe, ya está confirmado o la cuenta está desactivada, no se hace nada
        // y la respuesta es idéntica: así no se revela quién tiene cuenta.
        artesanoRepositoryPort.buscarPorCorreo(correo)
                .filter(artesano -> artesano.isActivo() && !artesano.isCorreoVerificado())
                .ifPresent(notificadorCuenta::enviarVerificacion);
    }
}