package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.Producto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository productoJpaRepository;
    private final ArtesanoJpaRepository artesanoJpaRepository;

    public ProductoRepositoryAdapter(ProductoJpaRepository productoJpaRepository,
                                      ArtesanoJpaRepository artesanoJpaRepository) {
        this.productoJpaRepository = productoJpaRepository;
        this.artesanoJpaRepository = artesanoJpaRepository;
    }

    @Override
    public Producto guardar(Producto producto) {
        ArtesanoEntity artesano = artesanoJpaRepository.findById(producto.getIdArtesano())
                .orElseThrow(() -> new IllegalArgumentException("El artesano indicado no existe."));

        ProductoEntity entity = new ProductoEntity(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getCantidad(),
                artesano
        );

        ProductoEntity guardado = productoJpaRepository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return productoJpaRepository.findById(id)
                .map(this::aDominio);
    }

    @Override
    public List<Producto> listarPorArtesano(Long idArtesano) {
        return productoJpaRepository.findByArtesanoId(idArtesano)
                .stream()
                .map(this::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        productoJpaRepository.deleteById(id);
    }

    private Producto aDominio(ProductoEntity entity) {
        return new Producto(
                entity.getId(),
                entity.getNombre(),
                entity.getPrecio(),
                entity.getCantidad(),
                entity.getArtesano().getId()
        );
    }
}