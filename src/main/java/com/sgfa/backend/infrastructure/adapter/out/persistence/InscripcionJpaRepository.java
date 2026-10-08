package com.sgfa.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionJpaRepository extends JpaRepository<InscripcionEntity, Long> {

    List<InscripcionEntity> findByArtesanoId(Long idArtesano);

    List<InscripcionEntity> findByEdicionFeriaIdAndEstado(Long idEdicionFeria, String estado);

    boolean existsByArtesanoIdAndEdicionFeriaId(Long idArtesano, Long idEdicionFeria);
}