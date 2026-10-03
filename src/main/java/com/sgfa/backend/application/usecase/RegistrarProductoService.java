package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.RegistrarProductoUseCase;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.Producto;

import java.math.BigDecimal;

public class RegistrarProductoService implements RegistrarProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;

    public RegistrarProductoService(ProductoRepositoryPort productoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
    }

    @Override
    public Producto registrar(String nombre, BigDecimal precio, Integer cantidad, Long idArtesano) {
        Producto nuevoProducto = new Producto(null, nombre, precio, cantidad, idArtesano);
        return productoRepositoryPort.guardar(nuevoProducto);
    }
}