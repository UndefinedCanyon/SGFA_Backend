package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Producto;

import java.math.BigDecimal;

public interface ActualizarProductoUseCase {

    Producto actualizar(Long idProducto, String nombre, BigDecimal precio, Integer cantidad, Long idArtesanoAutenticado);
}