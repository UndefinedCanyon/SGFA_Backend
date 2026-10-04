package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.InscripcionDuplicadaException;
import com.sgfa.backend.application.port.in.AprobarSolicitudUseCase;
import com.sgfa.backend.application.port.in.ConsultarSolicitudesUseCase;
import com.sgfa.backend.application.port.in.RechazarSolicitudUseCase;
import com.sgfa.backend.application.port.in.SolicitarParticipacionUseCase;
import com.sgfa.backend.domain.model.Inscripcion;
import com.sgfa.backend.infrastructure.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final SolicitarParticipacionUseCase solicitarParticipacionUseCase;
    private final AprobarSolicitudUseCase aprobarSolicitudUseCase;
    private final RechazarSolicitudUseCase rechazarSolicitudUseCase;
    private final ConsultarSolicitudesUseCase consultarSolicitudesUseCase;

    public InscripcionController(SolicitarParticipacionUseCase solicitarParticipacionUseCase,
                                  AprobarSolicitudUseCase aprobarSolicitudUseCase,
                                  RechazarSolicitudUseCase rechazarSolicitudUseCase,
                                  ConsultarSolicitudesUseCase consultarSolicitudesUseCase) {
        this.solicitarParticipacionUseCase = solicitarParticipacionUseCase;
        this.aprobarSolicitudUseCase = aprobarSolicitudUseCase;
        this.rechazarSolicitudUseCase = rechazarSolicitudUseCase;
        this.consultarSolicitudesUseCase = consultarSolicitudesUseCase;
    }

    private UsuarioAutenticado obtenerUsuarioAutenticado() {
        return (UsuarioAutenticado) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @PostMapping
    public Inscripcion solicitar(@RequestBody Map<String, Object> body) {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        Long idEdicionFeria = Long.valueOf(body.get("idEdicionFeria").toString());
        return solicitarParticipacionUseCase.solicitar(usuario.getId(), idEdicionFeria);
    }

    @GetMapping("/mias")
    public List<Inscripcion> consultarMisSolicitudes() {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        return consultarSolicitudesUseCase.consultarPorArtesano(usuario.getId());
    }

    @GetMapping
    public List<Inscripcion> consultarTodas() {
        return consultarSolicitudesUseCase.consultarTodas();
    }

    @PutMapping("/{id}/aprobar")
    public Inscripcion aprobar(@PathVariable Long id) {
        return aprobarSolicitudUseCase.aprobar(id);
    }

    @PutMapping("/{id}/rechazar")
    public Inscripcion rechazar(@PathVariable Long id) {
        return rechazarSolicitudUseCase.rechazar(id);
    }

    @ExceptionHandler(InscripcionDuplicadaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> manejarInscripcionDuplicada(InscripcionDuplicadaException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> manejarNoEncontrado(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}