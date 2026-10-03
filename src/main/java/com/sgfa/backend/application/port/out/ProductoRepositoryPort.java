package com.sgfa.backend.application.port.out;

import com.sgfa.backend.domain.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {

    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(Long id);

    List<Producto> listarPorArtesano(Long idArtesano);

    void eliminar(Long id);
}