package com.sgfa.backend.application.port.out;

import com.sgfa.backend.domain.model.Administrador;

import java.util.Optional;

public interface AdministradorRepositoryPort {

    Optional<Administrador> buscarPorCorreo(String correoElectronico);
}