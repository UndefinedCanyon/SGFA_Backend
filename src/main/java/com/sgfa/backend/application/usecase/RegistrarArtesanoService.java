package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.application.port.out.AdministradorRepositoryPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import org.springframework.security.crypto.password.PasswordEncoder;

public class RegistrarArtesanoService implements RegistrarArtesanoUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final AdministradorRepositoryPort administradorRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public RegistrarArtesanoService(ArtesanoRepositoryPort artesanoRepositoryPort,AdministradorRepositoryPort administradorRepositoryPort,PasswordEncoder passwordEncoder) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.administradorRepositoryPort = administradorRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Artesano registrar(String nombre, String correoElectronico, String contrasena,String cc, String telefono, String nombreEmprendimiento,String descripcionCorta) {

        if (artesanoRepositoryPort.existePorCorreo(correoElectronico)) {
            throw new DatoDuplicadoException("Ya existe un artesano registrado con ese correo electrónico.");
        }

        if (artesanoRepositoryPort.existePorCc(cc)) {
            throw new DatoDuplicadoException("Ya existe un artesano registrado con esa cédula.");
        }

        if (administradorRepositoryPort.buscarPorCorreo(correoElectronico).isPresent()) {
            throw new DatoDuplicadoException("Ese correo electrónico no está disponible.");
        }

        String contrasenaCifrada = passwordEncoder.encode(contrasena);

        Artesano nuevoArtesano = new Artesano(null, nombre, correoElectronico, contrasenaCifrada,
                cc, telefono, nombreEmprendimiento, descripcionCorta);

        return artesanoRepositoryPort.guardar(nuevoArtesano);
    }
}