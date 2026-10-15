package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.SesionIniciada;

public interface IniciarSesionUseCase {

    SesionIniciada iniciarSesion(String correoElectronico, String contrasena);
}