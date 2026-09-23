package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.domain.model.Lugar;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LugarRepositoryAdapter implements LugarRepositoryPort {

    private final LugarJpaRepository lugarJpaRepository;

    public LugarRepositoryAdapter(LugarJpaRepository lugarJpaRepository) {
        this.lugarJpaRepository = lugarJpaRepository;
    }

    @Override
    public Lugar guardar(Lugar lugar) {
        LugarEntity entity = new LugarEntity(lugar.getId(), lugar.getNombre(), lugar.getDireccion());
        LugarEntity guardada = lugarJpaRepository.save(entity);
        return aDominio(guardada);
    }

    @Override
    public Optional<Lugar> buscarPorId(Long id) {
        return lugarJpaRepository.findById(id)
                .map(this::aDominio);
    }

    @Override
    public List<Lugar> listarTodos() {
        return lugarJpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .collect(Collectors.toList());
    }

    private Lugar aDominio(LugarEntity entity) {
        return new Lugar(entity.getId(), entity.getNombre(), entity.getDireccion());
    }
}