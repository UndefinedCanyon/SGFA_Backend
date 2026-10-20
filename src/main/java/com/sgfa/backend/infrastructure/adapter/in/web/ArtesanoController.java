package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.DatoDuplicadoException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarPerfilArtesanoUseCase;
import com.sgfa.backend.application.port.in.CambiarEstadoArtesanoUseCase;
import com.sgfa.backend.application.port.in.ConsultarArtesanosUseCase;
import com.sgfa.backend.application.port.in.GestionarLogoArtesanoUseCase;
import com.sgfa.backend.application.port.in.RegistrarArtesanoUseCase;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.infrastructure.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final GestionarLogoArtesanoUseCase gestionarLogoArtesanoUseCase;

    public ArtesanoController(RegistrarArtesanoUseCase registrarArtesanoUseCase,
                               CambiarEstadoArtesanoUseCase cambiarEstadoArtesanoUseCase,
                               ConsultarArtesanosUseCase consultarArtesanosUseCase,
                               ActualizarPerfilArtesanoUseCase actualizarPerfilArtesanoUseCase,
                               GestionarLogoArtesanoUseCase gestionarLogoArtesanoUseCase) {
        this.registrarArtesanoUseCase = registrarArtesanoUseCase;
        this.cambiarEstadoArtesanoUseCase = cambiarEstadoArtesanoUseCase;
        this.consultarArtesanosUseCase = consultarArtesanosUseCase;
        this.actualizarPerfilArtesanoUseCase = actualizarPerfilArtesanoUseCase;
        this.gestionarLogoArtesanoUseCase = gestionarLogoArtesanoUseCase;
    }

    private UsuarioAutenticado obtenerUsuarioAutenticado() {
        return (UsuarioAutenticado) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private byte[] leerBytes(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new DatoInvalidoException("No se pudo leer el archivo enviado.");
        }
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
        respuesta.put("logo", artesano.getLogo());
        respuesta.put("correoVerificado", artesano.isCorreoVerificado());
        return respuesta;
    }

    private Artesano registrar(Map<String, String> body, boolean correoVerificado) {
        return registrarArtesanoUseCase.registrar(
                body.get("nombre"),
                body.get("correoElectronico"),
                body.get("contrasena"),
                body.get("cc"),
                body.get("telefono"),
                body.get("nombreEmprendimiento"),
                body.get("descripcionCorta"),
                correoVerificado
        );
    }

    /** Registro público: la cuenta queda pendiente hasta que el artesano confirme su correo. */
    @PostMapping
    public Map<String, Object> registrarPublico(@RequestBody Map<String, String> body) {
        return aRespuesta(registrar(body, false));
    }

    /** Registro por el administrador: la cuenta queda confirmada y no se envía correo. */
    @PostMapping("/admin")
    public Map<String, Object> registrarPorAdministrador(@RequestBody Map<String, String> body) {
        return aRespuesta(registrar(body, true));
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

    @PutMapping("/perfil/logo")
    public Map<String, Object> subirLogo(@RequestParam("archivo") MultipartFile archivo) {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        return aRespuesta(gestionarLogoArtesanoUseCase.subirLogo(usuario.getId(), leerBytes(archivo)));
    }

    @DeleteMapping("/perfil/logo")
    public Map<String, Object> quitarLogo() {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        return aRespuesta(gestionarLogoArtesanoUseCase.quitarLogo(usuario.getId()));
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