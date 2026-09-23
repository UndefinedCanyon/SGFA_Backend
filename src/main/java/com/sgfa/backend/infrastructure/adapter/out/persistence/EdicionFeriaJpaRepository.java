package com.sgfa.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EdicionFeriaJpaRepository extends JpaRepository<EdicionFeriaEntity, Long> {

    List<EdicionFeriaEntity> findByFeriaId(Long idFeria);
}