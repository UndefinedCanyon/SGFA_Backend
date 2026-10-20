package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.ImagenAlmacenada;

import java.util.Optional;

public interface ConsultarImagenUseCase {

    Optional<ImagenAlmacenada> consultar(String nombre);
}