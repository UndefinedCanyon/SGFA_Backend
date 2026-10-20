package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Artesano;

public interface GestionarLogoArtesanoUseCase {

    Artesano subirLogo(Long idArtesano, byte[] contenido);

    Artesano quitarLogo(Long idArtesano);
}