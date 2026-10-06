package com.sgfa.backend.application.port.in;

public interface CambiarEstadoEdicionFeriaUseCase {

    void cambiarEstado(Long idEdicionFeria, boolean activo);
}