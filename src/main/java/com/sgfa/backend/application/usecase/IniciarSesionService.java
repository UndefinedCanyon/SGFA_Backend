package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.CorreoNoVerificadoException;
import com.sgfa.backend.application.exception.CredencialesInvalidasException;
import com.sgfa.backend.application.port.in.IniciarSesionUseCase;
import com.sgfa.backend.application.port.out.AdministradorRepositoryPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.CifradoContrasenaPort;
import com.sgfa.backend.application.port.out.GeneradorTokenPort;
import com.sgfa.backend.domain.model.Administrador;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.SesionIniciada;

import java.util.Optional;

public class IniciarSesionService implements IniciarSesionUseCase {

    private static final String MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos.";

    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final AdministradorRepositoryPort administradorRepositoryPort;
    private final CifradoContrasenaPort cifradoContrasenaPort;
    private final GeneradorTokenPort generadorTokenPort;

    public IniciarSesionService(ArtesanoRepositoryPort artesanoRepositoryPort,
                                 AdministradorRepositoryPort administradorRepositoryPort,
                                 CifradoContrasenaPort cifradoContrasenaPort,
                                 GeneradorTokenPort generadorTokenPort) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.administradorRepositoryPort = administradorRepositoryPort;
        this.cifradoContrasenaPort = cifradoContrasenaPort;
        this.generadorTokenPort = generadorTokenPort;
    }

    @Override
    public SesionIniciada iniciarSesion(String correoElectronico, String contrasena) {
        String correo = DatosCuenta.normalizarCorreo(correoElectronico);

        Optional<Artesano> artesanoEncontrado = artesanoRepositoryPort.buscarPorCorreo(correo);

        if (artesanoEncontrado.isPresent()) {
            Artesano artesano = artesanoEncontrado.get();

            if (!artesano.isActivo()
                    || !cifradoContrasenaPort.coincide(contrasena, artesano.getContrasena())) {
                throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES);
            }

            if (!artesano.isCorreoVerificado()) {
                throw new CorreoNoVerificadoException(
                        "Debes confirmar tu correo electrónico antes de iniciar sesión. Revisa tu bandeja de entrada.");
            }

            String token = generadorTokenPort.generar(artesano.getCorreoElectronico(), "ARTESANO", artesano.getId());
            return new SesionIniciada(artesano.getId(), artesano.getNombre(),
                    artesano.getCorreoElectronico(), "ARTESANO", token);
        }

        Optional<Administrador> adminEncontrado = administradorRepositoryPort.buscarPorCorreo(correo);

        if (adminEncontrado.isPresent()) {
            Administrador admin = adminEncontrado.get();

            if (!cifradoContrasenaPort.coincide(contrasena, admin.getContrasena())) {
                throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES);
            }

            String token = generadorTokenPort.generar(admin.getCorreoElectronico(), "ADMINISTRADOR", admin.getId());
            return new SesionIniciada(admin.getId(), admin.getNombre(),
                    admin.getCorreoElectronico(), "ADMINISTRADOR", token);
        }

        throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES);
    }
}