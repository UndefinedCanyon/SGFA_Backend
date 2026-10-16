package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.application.port.out.AdministradorRepositoryPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.CifradoContrasenaPort;
import com.sgfa.backend.domain.model.Artesano;

public class RegistrarArtesanoService implements RegistrarArtesanoUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final AdministradorRepositoryPort administradorRepositoryPort;
    private final CifradoContrasenaPort cifradoContrasenaPort;

    public RegistrarArtesanoService(ArtesanoRepositoryPort artesanoRepositoryPort,
                                     AdministradorRepositoryPort administradorRepositoryPort,
                                     CifradoContrasenaPort cifradoContrasenaPort) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.administradorRepositoryPort = administradorRepositoryPort;
        this.cifradoContrasenaPort = cifradoContrasenaPort;
    }

    @Override
    public Artesano registrar(String nombre, String correoElectronico, String contrasena,
                               String cc, String telefono, String nombreEmprendimiento,
                               String descripcionCorta) {

        if (artesanoRepositoryPort.existePorCorreo(correoElectronico)) {
            throw new DatoDuplicadoException("Ya existe un artesano registrado con ese correo electrónico.");
        }

        if (artesanoRepositoryPort.existePorCc(cc)) {
            throw new DatoDuplicadoException("Ya existe un artesano registrado con esa cédula.");
        }

        if (administradorRepositoryPort.buscarPorCorreo(correoElectronico).isPresent()) {
            throw new DatoDuplicadoException("Ese correo electrónico no está disponible.");
        }

        String contrasenaCifrada = cifradoContrasenaPort.cifrar(contrasena);

        Artesano nuevoArtesano = new Artesano(null, nombre, correoElectronico, contrasenaCifrada,
                cc, telefono, nombreEmprendimiento, descripcionCorta, true);

        return artesanoRepositoryPort.guardar(nuevoArtesano);
    }
}