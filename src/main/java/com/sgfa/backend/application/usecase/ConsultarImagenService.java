package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.ConsultarImagenUseCase;
import com.sgfa.backend.application.port.out.AlmacenImagenesPort;
import com.sgfa.backend.domain.model.ImagenAlmacenada;
import com.sgfa.backend.domain.model.TipoImagen;

import java.util.Optional;

public class ConsultarImagenService implements ConsultarImagenUseCase {

    private final AlmacenImagenesPort almacenImagenesPort;

    public ConsultarImagenService(AlmacenImagenesPort almacenImagenesPort) {
        this.almacenImagenesPort = almacenImagenesPort;
    }

    @Override
    public Optional<ImagenAlmacenada> consultar(String nombre) {
        int punto = nombre.lastIndexOf('.');
        if (punto < 0) {
            return Optional.empty();
        }

        Optional<TipoImagen> tipo = TipoImagen.dePorExtension(nombre.substring(punto + 1));
        if (tipo.isEmpty()) {
            return Optional.empty();
        }

        return almacenImagenesPort.cargar(nombre)
                .map(contenido -> new ImagenAlmacenada(contenido, tipo.get()));
    }
}