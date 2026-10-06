package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.port.in.CambiarEstadoArtesanoUseCase;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.domain.model.Artesano;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/artesanos")
public class ArtesanoController {

    private final RegistrarArtesanoUseCase registrarArtesanoUseCase;
    private final CambiarEstadoArtesanoUseCase cambiarEstadoArtesanoUseCase;

    public ArtesanoController(RegistrarArtesanoUseCase registrarArtesanoUseCase,
                               CambiarEstadoArtesanoUseCase cambiarEstadoArtesanoUseCase) {
        this.registrarArtesanoUseCase = registrarArtesanoUseCase;
        this.cambiarEstadoArtesanoUseCase = cambiarEstadoArtesanoUseCase;
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

    @PutMapping("/{id}/estado")
    public void cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        cambiarEstadoArtesanoUseCase.cambiarEstado(id, body.get("activo"));
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> manejarDatoDuplicado(DatoDuplicadoException ex) {
        return Map.of("error", ex.getMessage());
    }
}