package com.sgfa.backend.application.port.in;

public interface CambiarEstadoArtesanoUseCase {

    void cambiarEstado(Long idArtesano, boolean activo);
}