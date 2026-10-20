package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Producto;

public interface GestionarImagenProductoUseCase {

    Producto subirImagen(Long idProducto, byte[] contenido, Long idArtesanoAutenticado);

    Producto quitarImagen(Long idProducto, Long idArtesanoAutenticado);
}