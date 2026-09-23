package com.sgfa.backend.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorJpaRepository extends JpaRepository<AdministradorEntity, Long> {

    Optional<AdministradorEntity> findByCorreoElectronico(String correoElectronico);
}