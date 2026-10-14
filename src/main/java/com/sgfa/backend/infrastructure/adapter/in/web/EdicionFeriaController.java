package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.CambiarEstadoEdicionFeriaUseCase;
import com.sgfa.backend.application.port.in.ConfigurarCupoEdicionUseCase;
import com.sgfa.backend.application.port.in.ConsultarEdicionesFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarOcupacionEdicionUseCase;
import com.sgfa.backend.application.port.in.ConsultarParticipantesEdicionUseCase;
import com.sgfa.backend.application.port.in.CrearEdicionFeriaUseCase;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.OcupacionEdicion;
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
    private final ConfigurarCupoEdicionUseCase configurarCupoEdicionUseCase;
    private final ConsultarOcupacionEdicionUseCase consultarOcupacionEdicionUseCase;

    public EdicionFeriaController(CrearEdicionFeriaUseCase crearEdicionFeriaUseCase,
                                   ConsultarEdicionesFeriaUseCase consultarEdicionesFeriaUseCase,
                                   CambiarEstadoEdicionFeriaUseCase cambiarEstadoEdicionFeriaUseCase,
                                   ConsultarParticipantesEdicionUseCase consultarParticipantesEdicionUseCase,
                                   ConfigurarCupoEdicionUseCase configurarCupoEdicionUseCase,
                                   ConsultarOcupacionEdicionUseCase consultarOcupacionEdicionUseCase) {
        this.crearEdicionFeriaUseCase = crearEdicionFeriaUseCase;
        this.consultarEdicionesFeriaUseCase = consultarEdicionesFeriaUseCase;
        this.cambiarEstadoEdicionFeriaUseCase = cambiarEstadoEdicionFeriaUseCase;
        this.consultarParticipantesEdicionUseCase = consultarParticipantesEdicionUseCase;
        this.configurarCupoEdicionUseCase = configurarCupoEdicionUseCase;
        this.consultarOcupacionEdicionUseCase = consultarOcupacionEdicionUseCase;
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

    @GetMapping("/{id}/ocupacion")
    public OcupacionEdicion ocupacion(@PathVariable Long id) {
        return consultarOcupacionEdicionUseCase.consultar(id);
    }

    @PutMapping("/{id}/estado")
    public void cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        cambiarEstadoEdicionFeriaUseCase.cambiarEstado(id, body.get("activo"));
    }

    @PutMapping("/{id}/cupo")
    public EdicionFeria configurarCupo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object valor = body.get("cupoMaximo");
        Integer cupo = (valor == null || valor.toString().isBlank()) ? null : Integer.valueOf(valor.toString());
        return configurarCupoEdicionUseCase.configurar(id, cupo);
    }

    @ExceptionHandler(DatoInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarDatoInvalido(DatoInvalidoException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> manejarNoEncontrado(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}