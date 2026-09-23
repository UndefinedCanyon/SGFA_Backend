package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.CredencialesInvalidasException;
import com.sgfa.backend.application.port.in.IniciarSesionUseCase;
import com.sgfa.backend.domain.model.SesionIniciada;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IniciarSesionUseCase iniciarSesionUseCase;

    public AuthController(IniciarSesionUseCase iniciarSesionUseCase) {
        this.iniciarSesionUseCase = iniciarSesionUseCase;
    }

    @PostMapping("/login")
    public SesionIniciada login(@RequestBody Map<String, String> body) {
        String correoElectronico = body.get("correoElectronico");
        String contrasena = body.get("contrasena");
        return iniciarSesionUseCase.iniciarSesion(correoElectronico, contrasena);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return Map.of("error", ex.getMessage());
    }
}