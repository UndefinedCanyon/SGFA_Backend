package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.AccesoNoAutorizadoException;
import com.sgfa.backend.application.port.in.ActualizarProductoUseCase;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.Producto;

import java.math.BigDecimal;

public class ActualizarProductoService implements ActualizarProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;

    public ActualizarProductoService(ProductoRepositoryPort productoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
    }

    @Override
    public Producto actualizar(Long idProducto, String nombre, BigDecimal precio, Integer cantidad, Long idArtesanoAutenticado) {
        Producto producto = productoRepositoryPort.buscarPorId(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        if (!producto.getIdArtesano().equals(idArtesanoAutenticado)) {
            throw new AccesoNoAutorizadoException("No tienes permiso para modificar este producto.");
        }

        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setCantidad(cantidad);

        return productoRepositoryPort.guardar(producto);
    }
}