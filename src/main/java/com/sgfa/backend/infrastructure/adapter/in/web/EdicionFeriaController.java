package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.port.in.CambiarEstadoEdicionFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarEdicionesFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarParticipantesEdicionUseCase;
import com.sgfa.backend.application.port.in.CrearEdicionFeriaUseCase;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.ParticipantePublico;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/edicionesferia")
public class EdicionFeriaController {

    private final CrearEdicionFeriaUseCase crearEdicionFeriaUseCase;
    private final ConsultarEdicionesFeriaUseCase consultarEdicionesFeriaUseCase;
    private final CambiarEstadoEdicionFeriaUseCase cambiarEstadoEdicionFeriaUseCase;
    private final ConsultarParticipantesEdicionUseCase consultarParticipantesEdicionUseCase;

    public EdicionFeriaController(CrearEdicionFeriaUseCase crearEdicionFeriaUseCase,
                                   ConsultarEdicionesFeriaUseCase consultarEdicionesFeriaUseCase,
                                   CambiarEstadoEdicionFeriaUseCase cambiarEstadoEdicionFeriaUseCase,
                                   ConsultarParticipantesEdicionUseCase consultarParticipantesEdicionUseCase) {
        this.crearEdicionFeriaUseCase = crearEdicionFeriaUseCase;
        this.consultarEdicionesFeriaUseCase = consultarEdicionesFeriaUseCase;
        this.cambiarEstadoEdicionFeriaUseCase = cambiarEstadoEdicionFeriaUseCase;
        this.consultarParticipantesEdicionUseCase = consultarParticipantesEdicionUseCase;
    }

    @PostMapping
    public EdicionFeria crear(@RequestBody Map<String, Object> body) {
        Long idFeria = Long.valueOf(body.get("idFeria").toString());
        Long idLugar = Long.valueOf(body.get("idLugar").toString());
        LocalDate fechaInicio = LocalDate.parse((String) body.get("fechaInicio"));
        LocalDate fechaFin = LocalDate.parse((String) body.get("fechaFin"));
        return crearEdicionFeriaUseCase.crear(idFeria, idLugar, fechaInicio, fechaFin);
    }

    @GetMapping
    public List<EdicionFeria> listar(@RequestParam(required = false) Long idFeria) {
        if (idFeria != null) {
            return consultarEdicionesFeriaUseCase.consultarPorFeria(idFeria);
        }
        return consultarEdicionesFeriaUseCase.consultarTodas();
    }

    @GetMapping("/todas")
    public List<EdicionFeria> listarTodasIncluyendoInactivas() {
        return consultarEdicionesFeriaUseCase.consultarTodasIncluyendoInactivas();
    }

    @GetMapping("/{id}/participantes")
    public List<ParticipantePublico> participantes(@PathVariable Long id) {
        return consultarParticipantesEdicionUseCase.consultar(id);
    }

    @PutMapping("/{id}/estado")
    public void cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        cambiarEstadoEdicionFeriaUseCase.cambiarEstado(id, body.get("activo"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> manejarNoEncontrado(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}