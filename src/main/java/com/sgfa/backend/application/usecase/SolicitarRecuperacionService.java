package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.SolicitarRecuperacionUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;

public class SolicitarRecuperacionService implements SolicitarRecuperacionUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final NotificadorCuenta notificadorCuenta;

    public SolicitarRecuperacionService(ArtesanoRepositoryPort artesanoRepositoryPort,
                                         NotificadorCuenta notificadorCuenta) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.notificadorCuenta = notificadorCuenta;
    }

    @Override
    public void solicitar(String correoElectronico) {
        String correo = DatosCuenta.normalizarCorreo(correoElectronico);
        if (correo.isEmpty()) {
            throw new DatoInvalidoException("Indica tu correo electrónico.");
        }

        artesanoRepositoryPort.buscarPorCorreo(correo)
                .filter(artesano -> artesano.isActivo())
                .ifPresent(notificadorCuenta::enviarRecuperacion);
    }
}