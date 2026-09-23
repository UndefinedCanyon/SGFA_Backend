package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.port.in.CrearEdicionFeriaUseCase;
import com.sgfa.backend.application.port.in.ConsultarEdicionesFeriaUseCase;
import com.sgfa.backend.domain.model.EdicionFeria;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/edicionesferia")
public class EdicionFeriaController {

    private final CrearEdicionFeriaUseCase crearEdicionFeriaUseCase;
    private final ConsultarEdicionesFeriaUseCase consultarEdicionesFeriaUseCase;

    public EdicionFeriaController(CrearEdicionFeriaUseCase crearEdicionFeriaUseCase,
                                   ConsultarEdicionesFeriaUseCase consultarEdicionesFeriaUseCase) {
        this.crearEdicionFeriaUseCase = crearEdicionFeriaUseCase;
        this.consultarEdicionesFeriaUseCase = consultarEdicionesFeriaUseCase;
    }

    @PostMapping
    public EdicionFeria crear(@RequestBody Map<String, Object> body) {
        Long idFeria = Long.valueOf(body.get("idFeria").toString());
        Long idLugar = Long.valueOf(body.get("idLugar").toString());
        LocalDate fechaInicio = LocalDate.parse((String) body.get("fechaInicio"));
        LocalDate fechaFin = LocalDate.parse((String) body.get("fechaFin"));
        return crearEdicionFeriaUseCase.crear(idFeria, idLugar, fechaInicio, fechaFin);
    }

    @GetMapping
    public List<EdicionFeria> listar(@RequestParam(required = false) Long idFeria) {
        if (idFeria != null) {
            return consultarEdicionesFeriaUseCase.consultarPorFeria(idFeria);
        }
        return consultarEdicionesFeriaUseCase.consultarTodas();
    }
}