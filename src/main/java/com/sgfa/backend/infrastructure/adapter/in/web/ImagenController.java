package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.port.in.ConsultarImagenUseCase;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    private final ConsultarImagenUseCase consultarImagenUseCase;

    public ImagenController(ConsultarImagenUseCase consultarImagenUseCase) {
        this.consultarImagenUseCase = consultarImagenUseCase;
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<byte[]> obtener(@PathVariable String nombre) {
        return consultarImagenUseCase.consultar(nombre)
                .map(imagen -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(imagen.getTipo().getTipoMime()))
                        // El nombre es aleatorio y nunca se reutiliza, así que el contenido no cambia.
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                        .body(imagen.getContenido()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}