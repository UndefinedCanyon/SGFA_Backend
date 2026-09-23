package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.port.in.CrearLugarUseCase;
import com.sgfa.backend.application.port.in.ConsultarLugaresUseCase;
import com.sgfa.backend.domain.model.Lugar;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lugares")
public class LugarController {

    private final CrearLugarUseCase crearLugarUseCase;
    private final ConsultarLugaresUseCase consultarLugaresUseCase;

    public LugarController(CrearLugarUseCase crearLugarUseCase,
                            ConsultarLugaresUseCase consultarLugaresUseCase) {
        this.crearLugarUseCase = crearLugarUseCase;
        this.consultarLugaresUseCase = consultarLugaresUseCase;
    }

    @PostMapping
    public Lugar crear(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        String direccion = body.get("direccion");
        return crearLugarUseCase.crear(nombre, direccion);
    }

    @GetMapping
    public List<Lugar> listar() {
        return consultarLugaresUseCase.consultarTodos();
    }
}