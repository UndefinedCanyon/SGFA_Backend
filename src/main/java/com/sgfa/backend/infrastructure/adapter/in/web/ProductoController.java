package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.AccesoNoAutorizadoException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.ActualizarProductoUseCase;
import com.sgfa.backend.application.port.in.ConsultarProductosUseCase;
import com.sgfa.backend.application.port.in.EliminarProductoUseCase;
import com.sgfa.backend.application.port.in.GestionarImagenProductoUseCase;
import com.sgfa.backend.application.port.in.RegistrarProductoUseCase;
import com.sgfa.backend.domain.model.Producto;
import com.sgfa.backend.infrastructure.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final RegistrarProductoUseCase registrarProductoUseCase;
    private final ActualizarProductoUseCase actualizarProductoUseCase;
    private final EliminarProductoUseCase eliminarProductoUseCase;
    private final ConsultarProductosUseCase consultarProductosUseCase;
    private final GestionarImagenProductoUseCase gestionarImagenProductoUseCase;

    public ProductoController(RegistrarProductoUseCase registrarProductoUseCase,
                               ActualizarProductoUseCase actualizarProductoUseCase,
                               EliminarProductoUseCase eliminarProductoUseCase,
                               ConsultarProductosUseCase consultarProductosUseCase,
                               GestionarImagenProductoUseCase gestionarImagenProductoUseCase) {
        this.registrarProductoUseCase = registrarProductoUseCase;
        this.actualizarProductoUseCase = actualizarProductoUseCase;
        this.eliminarProductoUseCase = eliminarProductoUseCase;
        this.consultarProductosUseCase = consultarProductosUseCase;
        this.gestionarImagenProductoUseCase = gestionarImagenProductoUseCase;
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

    @PostMapping
    public Producto registrar(@RequestBody Map<String, Object> body) {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();

        String nombre = (String) body.get("nombre");
        BigDecimal precio = new BigDecimal(body.get("precio").toString());
        Integer cantidad = Integer.valueOf(body.get("cantidad").toString());

        return registrarProductoUseCase.registrar(nombre, precio, cantidad, usuario.getId());
    }

    @GetMapping
    public List<Producto> listarMisProductos() {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        return consultarProductosUseCase.consultarPorArtesano(usuario.getId());
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();

        String nombre = (String) body.get("nombre");
        BigDecimal precio = new BigDecimal(body.get("precio").toString());
        Integer cantidad = Integer.valueOf(body.get("cantidad").toString());

        return actualizarProductoUseCase.actualizar(id, nombre, precio, cantidad, usuario.getId());
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        eliminarProductoUseCase.eliminar(id, usuario.getId());
    }

    @PutMapping("/{id}/imagen")
    public Producto subirImagen(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        return gestionarImagenProductoUseCase.subirImagen(id, leerBytes(archivo), usuario.getId());
    }

    @DeleteMapping("/{id}/imagen")
    public Producto quitarImagen(@PathVariable Long id) {
        UsuarioAutenticado usuario = obtenerUsuarioAutenticado();
        return gestionarImagenProductoUseCase.quitarImagen(id, usuario.getId());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> manejarAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
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