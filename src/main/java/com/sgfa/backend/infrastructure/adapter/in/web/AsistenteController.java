package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.AsistenteNoDisponibleException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.exception.LimiteUsoAsistenteException;
import com.sgfa.backend.application.port.in.AnalizarSolicitudesUseCase;
import com.sgfa.backend.domain.model.AnalisisSolicitudes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Asistente de IA para el administrador. Solo da sugerencias; las decisiones siguen siendo suyas. */
@RestController
@RequestMapping("/api/asistente")
public class AsistenteController {

    private final AnalizarSolicitudesUseCase analizarSolicitudesUseCase;

    public AsistenteController(AnalizarSolicitudesUseCase analizarSolicitudesUseCase) {
        this.analizarSolicitudesUseCase = analizarSolicitudesUseCase;
    }

    @PostMapping("/solicitudes/analisis")
    public AnalisisSolicitudes analizarSolicitudes(@RequestBody Map<String, Object> body) {
        Object id = body.get("idEdicionFeria");
        if (id == null) {
            throw new DatoInvalidoException("Indica la edición que quieres analizar.");
        }
        try {
            return analizarSolicitudesUseCase.analizar(Long.valueOf(id.toString()));
        } catch (NumberFormatException ex) {
            throw new DatoInvalidoException("La edición indicada no es válida.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> manejarNoEncontrado(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(LimiteUsoAsistenteException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, String> manejarLimite(LimiteUsoAsistenteException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(AsistenteNoDisponibleException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> manejarNoDisponible(AsistenteNoDisponibleException ex) {
        return Map.of("error", ex.getMessage());
    }
}
