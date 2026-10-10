package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Artesano;

public interface ActualizarPerfilArtesanoUseCase {

    Artesano actualizar(Long idArtesanoAutenticado, String nombre, String telefono,
                        String nombreEmprendimiento, String descripcionCorta);
}