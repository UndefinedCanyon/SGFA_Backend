package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.application.port.out.AdministradorRepositoryPort;
import com.sgfa.backend.domain.model.Administrador;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdministradorRepositoryAdapter implements AdministradorRepositoryPort {

    private final AdministradorJpaRepository administradorJpaRepository;

    public AdministradorRepositoryAdapter(AdministradorJpaRepository administradorJpaRepository) {
        this.administradorJpaRepository = administradorJpaRepository;
    }

    @Override
    public Optional<Administrador> buscarPorCorreo(String correoElectronico) {
        return administradorJpaRepository.findByCorreoElectronico(correoElectronico)
                .map(entity -> new Administrador(
                        entity.getId(),
                        entity.getNombre(),
                        entity.getCorreoElectronico(),
                        entity.getContrasena()
                ));
    }
}