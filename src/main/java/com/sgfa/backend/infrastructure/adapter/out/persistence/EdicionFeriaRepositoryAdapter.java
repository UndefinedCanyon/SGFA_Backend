package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EdicionFeriaRepositoryAdapter implements EdicionFeriaRepositoryPort {

    private final EdicionFeriaJpaRepository edicionFeriaJpaRepository;
    private final FeriaJpaRepository feriaJpaRepository;
    private final LugarJpaRepository lugarJpaRepository;

    public EdicionFeriaRepositoryAdapter(EdicionFeriaJpaRepository edicionFeriaJpaRepository,
                                          FeriaJpaRepository feriaJpaRepository,
                                          LugarJpaRepository lugarJpaRepository) {
        this.edicionFeriaJpaRepository = edicionFeriaJpaRepository;
        this.feriaJpaRepository = feriaJpaRepository;
        this.lugarJpaRepository = lugarJpaRepository;
    }

    @Override
    public EdicionFeria guardar(EdicionFeria edicionFeria) {
        FeriaEntity feria = feriaJpaRepository.findById(edicionFeria.getIdFeria())
                .orElseThrow(() -> new IllegalArgumentException("La feria indicada no existe."));

        LugarEntity lugar = lugarJpaRepository.findById(edicionFeria.getIdLugar())
                .orElseThrow(() -> new IllegalArgumentException("El lugar indicado no existe."));

        EdicionFeriaEntity entity = new EdicionFeriaEntity(
                edicionFeria.getId(),
                feria,
                lugar,
                edicionFeria.getFechaInicio(),
                edicionFeria.getFechaFin()
        );

        EdicionFeriaEntity guardada = edicionFeriaJpaRepository.save(entity);
        return aDominio(guardada);
    }

    @Override
    public Optional<EdicionFeria> buscarPorId(Long id) {
        return edicionFeriaJpaRepository.findById(id)
                .map(this::aDominio);
    }

    @Override
    public List<EdicionFeria> listarTodas() {
        return edicionFeriaJpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<EdicionFeria> listarPorFeria(Long idFeria) {
        return edicionFeriaJpaRepository.findByFeriaId(idFeria)
                .stream()
                .map(this::aDominio)
                .collect(Collectors.toList());
    }

    private EdicionFeria aDominio(EdicionFeriaEntity entity) {
        return new EdicionFeria(
                entity.getId(),
                entity.getFeria().getId(),
                entity.getLugar().getId(),
                entity.getFechaInicio(),
                entity.getFechaFin()
        );
    }
}