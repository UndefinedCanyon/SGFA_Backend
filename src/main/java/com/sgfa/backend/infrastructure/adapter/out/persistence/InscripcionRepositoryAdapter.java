package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.Inscripcion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InscripcionRepositoryAdapter implements InscripcionRepositoryPort {

    private final InscripcionJpaRepository inscripcionJpaRepository;
    private final ArtesanoJpaRepository artesanoJpaRepository;
    private final EdicionFeriaJpaRepository edicionFeriaJpaRepository;

    public InscripcionRepositoryAdapter(InscripcionJpaRepository inscripcionJpaRepository,
                                         ArtesanoJpaRepository artesanoJpaRepository,
                                         EdicionFeriaJpaRepository edicionFeriaJpaRepository) {
        this.inscripcionJpaRepository = inscripcionJpaRepository;
        this.artesanoJpaRepository = artesanoJpaRepository;
        this.edicionFeriaJpaRepository = edicionFeriaJpaRepository;
    }

    @Override
    public Inscripcion guardar(Inscripcion inscripcion) {
        ArtesanoEntity artesano = artesanoJpaRepository.findById(inscripcion.getIdArtesano())
                .orElseThrow(() -> new IllegalArgumentException("El artesano indicado no existe."));

        EdicionFeriaEntity edicionFeria = edicionFeriaJpaRepository.findById(inscripcion.getIdEdicionFeria())
                .orElseThrow(() -> new IllegalArgumentException("La edición de feria indicada no existe."));

        InscripcionEntity entity = new InscripcionEntity(
                inscripcion.getId(),
                artesano,
                edicionFeria,
                inscripcion.getFechaInscripcion(),
                inscripcion.getEstado()
        );

        InscripcionEntity guardada = inscripcionJpaRepository.save(entity);
        return aDominio(guardada);
    }

    @Override
    public Optional<Inscripcion> buscarPorId(Long id) {
        return inscripcionJpaRepository.findById(id)
                .map(this::aDominio);
    }

    @Override
    public List<Inscripcion> listarPorArtesano(Long idArtesano) {
        return inscripcionJpaRepository.findByArtesanoId(idArtesano)
                .stream()
                .map(this::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Inscripcion> listarTodas() {
        return inscripcionJpaRepository.findAll()
                .stream()
                .map(this::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existeInscripcion(Long idArtesano, Long idEdicionFeria) {
        return inscripcionJpaRepository.existsByArtesanoIdAndEdicionFeriaId(idArtesano, idEdicionFeria);
    }

    private Inscripcion aDominio(InscripcionEntity entity) {
        return new Inscripcion(
                entity.getId(),
                entity.getArtesano().getId(),
                entity.getEdicionFeria().getId(),
                entity.getFechaInscripcion(),
                entity.getEstado()
        );
    }
}