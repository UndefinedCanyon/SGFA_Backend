package com.sgfa.backend.infrastructure.adapter.in.web;

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

    public FeriaController(CrearFeriaUseCase crearFeriaUseCase,
                            ConsultarFeriasUseCase consultarFeriasUseCase) {
        this.crearFeriaUseCase = crearFeriaUseCase;
        this.consultarFeriasUseCase = consultarFeriasUseCase;
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
}