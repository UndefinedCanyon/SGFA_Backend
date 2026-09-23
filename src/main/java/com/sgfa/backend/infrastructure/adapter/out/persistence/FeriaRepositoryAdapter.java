package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.application.port.out.FeriaRepositoryPort;
import com.sgfa.backend.domain.model.Feria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FeriaRepositoryAdapter implements FeriaRepositoryPort {

    private final FeriaJpaRepository feriaJpaRepository;

    public FeriaRepositoryAdapter(FeriaJpaRepository feriaJpaRepository) {
        this.feriaJpaRepository = feriaJpaRepository;
    }

    @Override
    public Feria guardar(Feria feria) {
        FeriaEntity entity = new FeriaEntity(feria.getId(), feria.getNombreFeria(), feria.getIdAdmin());
        FeriaEntity guardada = feriaJpaRepository.save(entity);
        return aDominio(guardada);
    }

    @Override
    public Optional<Feria> buscarPorId(Long id) {
        return feriaJpaRepository.findById(id)
                .map(this::aDominio);
    }

    @Override
    public List<Feria> listarTodas() {
        return feriaJpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .collect(Collectors.toList());
    }

    private Feria aDominio(FeriaEntity entity) {
        return new Feria(entity.getId(), entity.getNombreFeria(), entity.getIdAdmin());
    }
}