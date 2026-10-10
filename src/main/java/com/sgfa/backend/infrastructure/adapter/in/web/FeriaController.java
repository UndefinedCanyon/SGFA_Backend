package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarFeriaUseCase;
import com.sgfa.backend.application.port.in.CambiarEstadoFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarFeriasUseCase;
import com.sgfa.backend.application.port.in.CrearFeriaUseCase;
import com.sgfa.backend.domain.model.Feria;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ferias")
public class FeriaController {

    private final CrearFeriaUseCase crearFeriaUseCase;
    private final ConsultarFeriasUseCase consultarFeriasUseCase;
    private final CambiarEstadoFeriaUseCase cambiarEstadoFeriaUseCase;
    private final ActualizarFeriaUseCase actualizarFeriaUseCase;

    public FeriaController(CrearFeriaUseCase crearFeriaUseCase,
                            ConsultarFeriasUseCase consultarFeriasUseCase,
                            CambiarEstadoFeriaUseCase cambiarEstadoFeriaUseCase,
                            ActualizarFeriaUseCase actualizarFeriaUseCase) {
        this.crearFeriaUseCase = crearFeriaUseCase;
        this.consultarFeriasUseCase = consultarFeriasUseCase;
        this.cambiarEstadoFeriaUseCase = cambiarEstadoFeriaUseCase;
        this.actualizarFeriaUseCase = actualizarFeriaUseCase;
    }

    @PostMapping
    public Feria crear(@RequestBody Map<String, Object> body) {
        String nombreFeria = (String) body.get("nombreFeria");
        Long idAdmin = Long.valueOf(body.get("idAdmin").toString());
        return crearFeriaUseCase.crear(nombreFeria, idAdmin);
    }

    @GetMapping
    public List<Feria> listar() {
        return consultarFeriasUseCase.consultarTodas();
    }

    @GetMapping("/todas")
    public List<Feria> listarTodasIncluyendoInactivas() {
        return consultarFeriasUseCase.consultarTodasIncluyendoInactivas();
    }

    @PutMapping("/{id}")
    public Feria actualizar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return actualizarFeriaUseCase.actualizar(id, body.get("nombreFeria"));
    }

    @PutMapping("/{id}/estado")
    public void cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        cambiarEstadoFeriaUseCase.cambiarEstado(id, body.get("activo"));
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