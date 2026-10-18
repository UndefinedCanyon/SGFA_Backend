package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarLugarUseCase;
import com.sgfa.backend.application.port.in.ConsultarLugaresUseCase;
import com.sgfa.backend.application.port.in.CrearLugarUseCase;
import com.sgfa.backend.domain.model.Lugar;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lugares")
public class LugarController {

    private final CrearLugarUseCase crearLugarUseCase;
    private final ConsultarLugaresUseCase consultarLugaresUseCase;
    private final ActualizarLugarUseCase actualizarLugarUseCase;

    public LugarController(CrearLugarUseCase crearLugarUseCase,
                            ConsultarLugaresUseCase consultarLugaresUseCase,
                            ActualizarLugarUseCase actualizarLugarUseCase) {
        this.crearLugarUseCase = crearLugarUseCase;
        this.consultarLugaresUseCase = consultarLugaresUseCase;
        this.actualizarLugarUseCase = actualizarLugarUseCase;
    }

    @PostMapping
    public Lugar crear(@RequestBody Map<String, Object> body) {
        return crearLugarUseCase.crear(
                leerTexto(body, "nombre"),
                leerTexto(body, "direccion"),
                leerCoordenada(body, "latitud", "La latitud"),
                leerCoordenada(body, "longitud", "La longitud")
        );
    }

    @GetMapping
    public List<Lugar> listar() {
        return consultarLugaresUseCase.consultarTodos();
    }

    @PutMapping("/{id}")
    public Lugar actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return actualizarLugarUseCase.actualizar(
                id,
                leerTexto(body, "nombre"),
                leerTexto(body, "direccion"),
                leerCoordenada(body, "latitud", "La latitud"),
                leerCoordenada(body, "longitud", "La longitud")
        );
    }

    private String leerTexto(Map<String, Object> body, String clave) {
        Object valor = body.get(clave);
        return valor == null ? null : valor.toString();
    }

    private Double leerCoordenada(Map<String, Object> body, String clave, String etiqueta) {
        Object valor = body.get(clave);
        if (valor == null || valor.toString().isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(valor.toString().trim());
        } catch (NumberFormatException ex) {
            throw new DatoInvalidoException(etiqueta + " no es un número válido.");
        }
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