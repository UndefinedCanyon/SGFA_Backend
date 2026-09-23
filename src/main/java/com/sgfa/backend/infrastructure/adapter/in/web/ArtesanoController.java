package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.domain.model.Artesano;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/artesanos")
public class ArtesanoController {

    private final RegistrarArtesanoUseCase registrarArtesanoUseCase;

    public ArtesanoController(RegistrarArtesanoUseCase registrarArtesanoUseCase) {
        this.registrarArtesanoUseCase = registrarArtesanoUseCase;
    }

    @PostMapping
    public Artesano registrar(@RequestBody Map<String, String> body) {
        return registrarArtesanoUseCase.registrar(
                body.get("nombre"),
                body.get("correoElectronico"),
                body.get("contrasena"),
                body.get("cc"),
                body.get("telefono"),
                body.get("nombreEmprendimiento"),
                body.get("descripcionCorta")
        );
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> manejarDatoDuplicado(DatoDuplicadoException ex) {
        return Map.of("error", ex.getMessage());
    }
}