package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarPerfilArtesanoUseCase;
import com.sgfa.backend.application.port.in.CambiarEstadoArtesanoUseCase;
import com.sgfa.backend.application.port.in.ConsultarArtesanosUseCase;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.infrastructure.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/artesanos")
public class ArtesanoController {

    private final RegistrarArtesanoUseCase registrarArtesanoUseCase;
    private final CambiarEstadoArtesanoUseCase cambiarEstadoArtesanoUseCase;
    private final ConsultarArtesanosUseCase consultarArtesanosUseCase;
    private final ActualizarPerfilArtesanoUseCase actualizarPerfilArtesanoUseCase;

    public ArtesanoController(RegistrarArtesanoUseCase registrarArtesanoUseCase,
                               CambiarEstadoArtesanoUseCase cambiarEstadoArtesanoUseCase,
                               ConsultarArtesanosUseCase consultarArtesanosUseCase,
                               ActualizarPerfilArtesanoUseCase actualizarPerfilArtesanoUseCase) {
        this.registrarArtesanoUseCase = registrarArtesanoUseCase;
        this.cambiarEstadoArtesanoUseCase = cambiarEstadoArtesanoUseCase;
        this.consultarArtesanosUseCase = consultarArtesanosUseCase;
        this.actualizarPerfilArtesanoUseCase = actualizarPerfilArtesanoUseCase;
    }

    private UsuarioAutenticado obtenerUsuarioAutenticado() {
        return (UsuarioAutenticado) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private Map<String, Object> aRespuesta(Artesano artesano) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", artesano.getId());
        respuesta.put("nombre", artesano.getNombre());
        respuesta.put("correoElectronico", artesano.getCorreoElectronico());
        respuesta.put("cc", artesano.getCc());
        respuesta.put("telefono", artesano.getTelefono());
        respuesta.put("nombreEmprendimiento", artesano.getNombreEmprendimiento());
        respuesta.put("descripcionCorta", artesano.getDescripcionCorta());
        respuesta.put("activo", artesano.isActivo());
        return respuesta;
    }

    @PostMapping
    public Map<String, Object> registrar(@RequestBody Map<String, String> body) {
        Artesano creado = registrarArtesanoUseCase.registrar(
                body.get("nombre"),
                body.get("correoElectronico"),
                body.get("contrasena"),
                body.get("cc"),
                body.get("telefono"),
                body.get("nombreEmprendimiento"),
                body.get("descripcionCorta")
        );
        return aRespuesta(creado);
    }

    @GetMapping
    public List<Map<String, Object>> listarTodos() {
        return consultarArtesanosUseCase.consultarTodos()
                .stream()
                .map(this::aRespuesta)
                .collect(Collectors.toList());
    }

    @GetMapping("/perfil")
    public Map<String, Object> miPerfil() {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        return aRespuesta(consultarArtesanosUseCase.consultarPorId(usuario.getId()));
    }

    @PutMapping("/perfil")
    public Map<String, Object> actualizarMiPerfil(@RequestBody Map<String, String> body) {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        Artesano actualizado = actualizarPerfilArtesanoUseCase.actualizar(
                usuario.getId(),
                body.get("nombre"),
                body.get("telefono"),
                body.get("nombreEmprendimiento"),
                body.get("descripcionCorta")
        );
        return aRespuesta(actualizado);
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