package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.AccesoNoAutorizadoException;
import com.sgfa.backend.application.port.in.EliminarProductoUseCase;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.Producto;

public class EliminarProductoService implements EliminarProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;

    public EliminarProductoService(ProductoRepositoryPort productoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
    }

    @Override
    public void eliminar(Long idProducto, Long idArtesanoAutenticado) {
        Producto producto = productoRepositoryPort.buscarPorId(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        if (!producto.getIdArtesano().equals(idArtesanoAutenticado)) {
            throw new AccesoNoAutorizadoException("No tienes permiso para eliminar este producto.");
        }

        productoRepositoryPort.eliminar(idProducto);
    }
}