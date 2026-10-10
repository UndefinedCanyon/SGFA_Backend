package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarPerfilArtesanoUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;

public class ActualizarPerfilArtesanoService implements ActualizarPerfilArtesanoUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;

    public ActualizarPerfilArtesanoService(ArtesanoRepositoryPort artesanoRepositoryPort) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
    }

    @Override
    public Artesano actualizar(Long idArtesanoAutenticado, String nombre, String telefono,
                               String nombreEmprendimiento, String descripcionCorta) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre es obligatorio.");
        }
        if (nombreEmprendimiento == null || nombreEmprendimiento.isBlank()) {
            throw new DatoInvalidoException("El nombre del emprendimiento es obligatorio.");
        }

        Artesano artesano = artesanoRepositoryPort.buscarPorId(idArtesanoAutenticado)
                .orElseThrow(() -> new IllegalArgumentException("El artesano no existe."));

        artesano.setNombre(nombre.trim());
        artesano.setTelefono(telefono);
        artesano.setNombreEmprendimiento(nombreEmprendimiento.trim());
        artesano.setDescripcionCorta(descripcionCorta);

        return artesanoRepositoryPort.guardar(artesano);
    }
}