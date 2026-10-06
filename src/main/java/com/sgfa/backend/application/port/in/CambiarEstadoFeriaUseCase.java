package com.sgfa.backend.application.port.in;

public interface CambiarEstadoFeriaUseCase {

    void cambiarEstado(Long idFeria, boolean activo);
}