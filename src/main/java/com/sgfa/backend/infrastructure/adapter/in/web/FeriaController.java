package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.port.in.CambiarEstadoFeriaUseCase;
import com.sgfa.backend.application.port.in.CrearFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarFeriasUseCase;
import com.sgfa.backend.domain.model.Feria;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ferias")
public class FeriaController {

    private final CrearFeriaUseCase crearFeriaUseCase;
    private final ConsultarFeriasUseCase consultarFeriasUseCase;
    private final CambiarEstadoFeriaUseCase cambiarEstadoFeriaUseCase;

    public FeriaController(CrearFeriaUseCase crearFeriaUseCase,
                            ConsultarFeriasUseCase consultarFeriasUseCase,
                            CambiarEstadoFeriaUseCase cambiarEstadoFeriaUseCase) {
        this.crearFeriaUseCase = crearFeriaUseCase;
        this.consultarFeriasUseCase = consultarFeriasUseCase;
        this.cambiarEstadoFeriaUseCase = cambiarEstadoFeriaUseCase;
    }

    @PutMapping("/{id}/estado")
    public void cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        cambiarEstadoFeriaUseCase.cambiarEstado(id, body.get("activo"));
    }

    @PostMapping
    public Feria crear(@RequestBody Map<String, Object> body) {
        String nombreFeria = (String) body.get("nombreFeria");
        Long idAdmin = Long.valueOf(body.get("idAdmin").toString());
        return crearFeriaUseCase.crear(nombreFeria, idAdmin);
    }

    @GetMapping("/todas")
    public List<Feria> listarTodasIncluyendoInactivas() {
        return consultarFeriasUseCase.consultarTodasIncluyendoInactivas();
    }
}