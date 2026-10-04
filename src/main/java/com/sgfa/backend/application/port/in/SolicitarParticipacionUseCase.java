package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Inscripcion;

public interface SolicitarParticipacionUseCase {

    Inscripcion solicitar(Long idArtesano, Long idEdicionFeria);
}