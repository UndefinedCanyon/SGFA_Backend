package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Artesano;

public interface RegistrarArtesanoUseCase {

    Artesano registrar(String nombre, String correoElectronico, String contrasena,
                        String cc, String telefono, String nombreEmprendimiento,
                        String descripcionCorta);
}