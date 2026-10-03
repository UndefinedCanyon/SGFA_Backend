package com.sgfa.backend.application.port.in;

public interface EliminarProductoUseCase {

    void eliminar(Long idProducto, Long idArtesanoAutenticado);
}