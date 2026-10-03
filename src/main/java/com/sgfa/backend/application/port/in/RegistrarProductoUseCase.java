package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Producto;

import java.math.BigDecimal;

public interface RegistrarProductoUseCase {

    Producto registrar(String nombre, BigDecimal precio, Integer cantidad, Long idArtesano);
}