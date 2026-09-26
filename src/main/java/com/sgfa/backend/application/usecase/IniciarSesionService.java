package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.CredencialesInvalidasException;
import com.sgfa.backend.application.port.in.IniciarSesionUseCase;
import com.sgfa.backend.application.port.out.AdministradorRepositoryPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.domain.model.Administrador;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.SesionIniciada;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class IniciarSesionService implements IniciarSesionUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final AdministradorRepositoryPort administradorRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public IniciarSesionService(ArtesanoRepositoryPort artesanoRepositoryPort,
                                 AdministradorRepositoryPort administradorRepositoryPort,
                                 PasswordEncoder passwordEncoder) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.administradorRepositoryPort = administradorRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SesionIniciada iniciarSesion(String correoElectronico, String contrasena) {

        Optional<Artesano> artesanoEncontrado = artesanoRepositoryPort.buscarPorCorreo(correoElectronico);

        if (artesanoEncontrado.isPresent()) {
            Artesano artesano = artesanoEncontrado.get();
            if (!passwordEncoder.matches(contrasena, artesano.getContrasena())) {
                throw new CredencialesInvalidasException("Correo o contraseña incorrectos.");
            }
            return new SesionIniciada(artesano.getId(), artesano.getNombre(),
                    artesano.getCorreoElectronico(), "ARTESANO");
        }

        Optional<Administrador> adminEncontrado = administradorRepositoryPort.buscarPorCorreo(correoElectronico);

        if (adminEncontrado.isPresent()) {
            Administrador admin = adminEncontrado.get();
            if (!passwordEncoder.matches(contrasena, admin.getContrasena())) {
                throw new CredencialesInvalidasException("Correo o contraseña incorrectos.");
            }
            return new SesionIniciada(admin.getId(), admin.getNombre(),
                    admin.getCorreoElectronico(), "ADMINISTRADOR");
        }

        throw new CredencialesInvalidasException("Correo o contraseña incorrectos.");
    }
}