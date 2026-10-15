package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;

public class RegistrarArtesanoService implements RegistrarArtesanoUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;

    public RegistrarArtesanoService(ArtesanoRepositoryPort artesanoRepositoryPort) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
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

        Artesano nuevoArtesano = new Artesano(null, nombre, correoElectronico, contrasena,
                cc, telefono, nombreEmprendimiento, descripcionCorta);

        return artesanoRepositoryPort.guardar(nuevoArtesano);
    }
}