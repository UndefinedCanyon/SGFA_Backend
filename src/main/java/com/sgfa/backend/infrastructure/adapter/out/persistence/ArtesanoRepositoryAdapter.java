package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ArtesanoRepositoryAdapter implements ArtesanoRepositoryPort {

    private final ArtesanoJpaRepository artesanoJpaRepository;

    public ArtesanoRepositoryAdapter(ArtesanoJpaRepository artesanoJpaRepository) {
        this.artesanoJpaRepository = artesanoJpaRepository;
    }

    @Override
    public Artesano guardar(Artesano artesano) {
        ArtesanoEntity entity = new ArtesanoEntity(
                artesano.getId(),
                artesano.getNombre(),
                artesano.getCorreoElectronico(),
                artesano.getContrasena(),
                artesano.getCc(),
                artesano.getTelefono(),
                artesano.getNombreEmprendimiento(),
                artesano.getDescripcionCorta()
        );
        ArtesanoEntity guardado = artesanoJpaRepository.save(entity);
        return aDominio(guardado);
    }

    @Override
    public Optional<Artesano> buscarPorId(Long id) {
        return artesanoJpaRepository.findById(id)
                .map(this::aDominio);
    }

    @Override
    public List<Artesano> listarTodos() {
        return artesanoJpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorCorreo(String correoElectronico) {
        return artesanoJpaRepository.existsByCorreoElectronico(correoElectronico);
    }

    @Override
    public boolean existePorCc(String cc) {
        return artesanoJpaRepository.existsByCc(cc);
    }

    private Artesano aDominio(ArtesanoEntity entity) {
        return new Artesano(
                entity.getId(),
                entity.getNombre(),
                entity.getCorreoElectronico(),
                entity.getContrasena(),
                entity.getCc(),
                entity.getTelefono(),
                entity.getNombreEmprendimiento(),
                entity.getDescripcionCorta()
        );
    }
}