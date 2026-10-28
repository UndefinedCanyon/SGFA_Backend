package com.sgfa.backend.application.port.in;

public interface CambiarEstadoLugarUseCase {

    void cambiarEstado(Long idLugar, boolean activo);
}