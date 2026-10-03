package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ConsultarProductosUseCase;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.Producto;

import java.util.List;

public class ConsultarProductosService implements ConsultarProductosUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;

    public ConsultarProductosService(ProductoRepositoryPort productoRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
    }

    @Override
    public List<Producto> consultarPorArtesano(Long idArtesano) {
        return productoRepositoryPort.listarPorArtesano(idArtesano);
    }
}