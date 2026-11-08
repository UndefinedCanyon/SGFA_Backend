package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.exception.AccesoNoAutorizadoException;
import com.sgfa.backend.application.port.in.ActualizarProductoUseCase;
import com.sgfa.backend.application.port.in.ConsultarProductosUseCase;
import com.sgfa.backend.application.port.in.EliminarProductoUseCase;
import com.sgfa.backend.application.port.in.GestionarImagenProductoUseCase;
import com.sgfa.backend.application.port.in.RegistrarProductoUseCase;
import com.sgfa.backend.domain.model.Producto;
import com.sgfa.backend.infrastructure.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductoControllerTest {

    private RegistrarProductoUseCase registrar;
    private ActualizarProductoUseCase actualizar;
    private EliminarProductoUseCase eliminar;
    private ConsultarProductosUseCase consultar;
    private GestionarImagenProductoUseCase imagenes;
    private MockMvc mvc;

    private static Producto producto(long id, String nombre, String precio, int cantidad) {
        return new Producto(id, nombre, new BigDecimal(precio), cantidad, 7L);
    }

    @BeforeEach
    void preparar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(7L, "ana@correo.com", "ARTESANO"), null, List.of()));
        registrar = mock(RegistrarProductoUseCase.class);
        actualizar = mock(ActualizarProductoUseCase.class);
        eliminar = mock(EliminarProductoUseCase.class);
        consultar = mock(ConsultarProductosUseCase.class);
        imagenes = mock(GestionarImagenProductoUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(
                new ProductoController(registrar, actualizar, eliminar, consultar, imagenes)).build();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/productos registra el producto del artesano de la sesión")
    void registrar_un_producto() throws Exception {
        when(registrar.registrar("Vasija", new BigDecimal("12000"), 3, 7L))
                .thenReturn(producto(1L, "Vasija", "12000", 3));

        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Vasija\",\"precio\":12000,\"cantidad\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Vasija"))
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$.idArtesano").value(7));
    }

    @Test
    @DisplayName("el precio y la cantidad también se aceptan como texto")
    void precio_y_cantidad_como_texto() throws Exception {
        when(registrar.registrar("Taza", new BigDecimal("9.99"), 2, 7L)).thenReturn(producto(2L, "Taza", "9.99", 2));

        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Taza\",\"precio\":\" 9.99 \",\"cantidad\":\"2\"}"))
                .andExpect(status().isOk());

        verify(registrar).registrar("Taza", new BigDecimal("9.99"), 2, 7L);
    }

    @Test
    @DisplayName("los campos que faltan llegan como nulos al caso de uso, que decide qué hacer")
    void los_campos_ausentes_llegan_como_nulos() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(registrar).registrar(null, null, null, 7L);
    }

    @Test
    @DisplayName("un precio que no es número responde 400 sin registrar nada")
    void precio_invalido_responde_400() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Vasija\",\"precio\":\"barato\",\"cantidad\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El precio no es un número válido."));

        verifyNoInteractions(registrar);
    }

    @Test
    @DisplayName("una cantidad que no es un entero responde 400 sin registrar nada")
    void cantidad_invalida_responde_400() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Vasija\",\"precio\":12000,\"cantidad\":\"muchas\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La cantidad debe ser un número entero."));

        verifyNoInteractions(registrar);
    }

    @Test
    @DisplayName("GET /api/productos lista solo los productos del artesano de la sesión")
    void listar_mis_productos() throws Exception {
        when(consultar.consultarPorArtesano(7L))
                .thenReturn(List.of(producto(1L, "Vasija", "12000", 3), producto(2L, "Taza", "8000", 5)));

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Taza"));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} actualiza el producto")
    void actualizar_un_producto() throws Exception {
        when(actualizar.actualizar(5L, "Taza", new BigDecimal("8000"), 2, 7L))
                .thenReturn(producto(5L, "Taza", "8000", 2));

        mvc.perform(put("/api/productos/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Taza\",\"precio\":8000,\"cantidad\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Taza"));
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} elimina el producto indicando quién lo pide")
    void eliminar_un_producto() throws Exception {
        mvc.perform(delete("/api/productos/5"))
                .andExpect(status().isOk());

        verify(eliminar).eliminar(5L, 7L);
    }

    @Test
    @DisplayName("tocar el producto de otro artesano responde 403")
    void producto_ajeno_responde_403() throws Exception {
        org.mockito.Mockito.doThrow(new AccesoNoAutorizadoException("Ese producto no es tuyo."))
                .when(eliminar).eliminar(5L, 7L);

        mvc.perform(delete("/api/productos/5"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Ese producto no es tuyo."));
    }

    @Test
    @DisplayName("un producto que no existe responde 404")
    void producto_inexistente_responde_404() throws Exception {
        when(actualizar.actualizar(99L, "X", new BigDecimal("1"), 1, 7L))
                .thenThrow(new IllegalArgumentException("El producto no existe."));

        mvc.perform(put("/api/productos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X\",\"precio\":1,\"cantidad\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El producto no existe."));
    }

    @Test
    @DisplayName("PUT /api/productos/{id}/imagen sube la imagen enviada")
    void subir_la_imagen() throws Exception {
        byte[] bytes = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
        Producto conImagen = new Producto(5L, "Taza", new BigDecimal("8000"), 2, 7L, "abc.png");
        when(imagenes.subirImagen(eq(5L), aryEq(bytes), eq(7L))).thenReturn(conImagen);

        mvc.perform(multipart(HttpMethod.PUT, "/api/productos/5/imagen")
                        .file(new MockMultipartFile("archivo", "foto.png", "image/png", bytes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagen").value("abc.png"));
    }

    @Test
    @DisplayName("DELETE /api/productos/{id}/imagen quita la imagen")
    void quitar_la_imagen() throws Exception {
        when(imagenes.quitarImagen(5L, 7L)).thenReturn(producto(5L, "Taza", "8000", 2));

        mvc.perform(delete("/api/productos/5/imagen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }
}