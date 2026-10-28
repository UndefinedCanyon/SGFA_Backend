package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarLugarUseCase;
import com.sgfa.backend.application.port.in.CambiarEstadoLugarUseCase;
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
    private final ActualizarLugarUseCase actualizarLugarUseCase;
    private final CambiarEstadoLugarUseCase cambiarEstadoLugarUseCase;
    private final ConsultarLugaresUseCase consultarLugaresUseCase;

    public LugarController(CrearLugarUseCase crearLugarUseCase,
                            ActualizarLugarUseCase actualizarLugarUseCase,
                            CambiarEstadoLugarUseCase cambiarEstadoLugarUseCase,
                            ConsultarLugaresUseCase consultarLugaresUseCase) {
        this.crearLugarUseCase = crearLugarUseCase;
        this.actualizarLugarUseCase = actualizarLugarUseCase;
        this.cambiarEstadoLugarUseCase = cambiarEstadoLugarUseCase;
        this.consultarLugaresUseCase = consultarLugaresUseCase;
    }

    @PostMapping
    public Lugar crear(@RequestBody Map<String, Object> body) {
        return crearLugarUseCase.crear(
                aTexto(body.get("nombre")),
                aTexto(body.get("direccion")),
                aNumero(body.get("latitud")),
                aNumero(body.get("longitud"))
        );
    }

    @PutMapping("/{id}")
    public Lugar actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return actualizarLugarUseCase.actualizar(
                id,
                aTexto(body.get("nombre")),
                aTexto(body.get("direccion")),
                aNumero(body.get("latitud")),
                aNumero(body.get("longitud"))
        );
    }

    @PutMapping("/{id}/estado")
    public void cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!(body.get("activo") instanceof Boolean activo)) {
            throw new DatoInvalidoException("Indica si el lugar queda activo o inactivo.");
        }
        cambiarEstadoLugarUseCase.cambiarEstado(id, activo);
    }

    @GetMapping
    public List<Lugar> listar() {
        return consultarLugaresUseCase.consultarTodos();
    }

    private String aTexto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private Double aNumero(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number numero) {
            return numero.doubleValue();
        }
        throw new DatoInvalidoException("La latitud y la longitud deben ser números.");
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