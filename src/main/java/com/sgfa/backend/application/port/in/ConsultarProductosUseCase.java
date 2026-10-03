package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Producto;

import java.util.List;

public interface ConsultarProductosUseCase {

    List<Producto> consultarPorArtesano(Long idArtesano);
}