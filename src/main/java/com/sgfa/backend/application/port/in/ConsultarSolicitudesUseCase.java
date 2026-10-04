package com.sgfa.backend.application.port.in;

import com.sgfa.backend.domain.model.Inscripcion;

import java.util.List;

public interface ConsultarSolicitudesUseCase {

    List<Inscripcion> consultarPorArtesano(Long idArtesano);

    List<Inscripcion> consultarTodas();
}