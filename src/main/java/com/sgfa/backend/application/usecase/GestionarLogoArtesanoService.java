package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.in.GestionarLogoArtesanoUseCase;
import com.sgfa.backend.application.port.out.AlmacenImagenesPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.TipoImagen;

public class GestionarLogoArtesanoService implements GestionarLogoArtesanoUseCase {

    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final AlmacenImagenesPort almacenImagenesPort;

    public GestionarLogoArtesanoService(ArtesanoRepositoryPort artesanoRepositoryPort,
                                         AlmacenImagenesPort almacenImagenesPort) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.almacenImagenesPort = almacenImagenesPort;
    }

    @Override
    public Artesano subirLogo(Long idArtesano, byte[] contenido) {
        Artesano artesano = buscar(idArtesano);
        TipoImagen tipo = ValidacionImagen.validar(contenido);

        String nuevo = almacenImagenesPort.guardar(contenido, tipo.getExtension());
        String anterior = artesano.getLogo();
        artesano.setLogo(nuevo);

        Artesano guardado;
        try {
            guardado = artesanoRepositoryPort.guardar(artesano);
        } catch (RuntimeException e) {
            almacenImagenesPort.eliminar(nuevo);
            throw e;
        }

        if (anterior != null) {
            almacenImagenesPort.eliminar(anterior);
        }
        return guardado;
    }

    @Override
    public Artesano quitarLogo(Long idArtesano) {
        Artesano artesano = buscar(idArtesano);

        String anterior = artesano.getLogo();
        if (anterior == null) {
            return artesano;
        }

        artesano.setLogo(null);
        Artesano guardado = artesanoRepositoryPort.guardar(artesano);
        almacenImagenesPort.eliminar(anterior);
        return guardado;
    }

    private Artesano buscar(Long idArtesano) {
        return artesanoRepositoryPort.buscarPorId(idArtesano)
                .orElseThrow(() -> new IllegalArgumentException("El artesano no existe."));
    }
}