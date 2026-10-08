package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.ParticipantePublico;

import java.util.List;

public interface ConsultarParticipantesEdicionUseCase {

    List<ParticipantePublico> consultar(Long idEdicionFeria);
}