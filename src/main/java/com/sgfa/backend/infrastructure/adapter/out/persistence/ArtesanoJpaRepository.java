package com.sgfa.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtesanoJpaRepository extends JpaRepository<ArtesanoEntity, Long> {

    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByCc(String cc);

    Optional<ArtesanoEntity> findByCorreoElectronico(String correoElectronico);
}