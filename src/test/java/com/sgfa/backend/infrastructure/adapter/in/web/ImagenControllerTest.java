package com.sgfa.backend.infrastructure.adapter.in.web;

import com.sgfa.backend.application.port.in.ConsultarImagenUseCase;
import com.sgfa.backend.domain.model.ImagenAlmacenada;
import com.sgfa.backend.domain.model.TipoImagen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImagenControllerTest {

    private ConsultarImagenUseCase consultar;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        consultar = mock(ConsultarImagenUseCase.class);
        mvc = MockMvcBuilders.standaloneSetup(new ImagenController(consultar)).build();
    }

    @Test
    @DisplayName("devuelve la imagen con su tipo y con caché de un año")
    void devuelve_la_imagen_con_su_tipo_y_cache() throws Exception {
        byte[] contenido = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
        when(consultar.consultar("abc123.png")).thenReturn(Optional.of(new ImagenAlmacenada(contenido, TipoImagen.PNG)));

        mvc.perform(get("/api/imagenes/abc123.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(contenido))
                .andExpect(header().string("Cache-Control", containsString("max-age=31536000")))
                .andExpect(header().string("Cache-Control", containsString("public")))
                .andExpect(header().string("Cache-Control", containsString("immutable")));
    }

    @Test
    @DisplayName("usa el tipo de contenido que corresponde a cada formato")
    void usa_el_tipo_de_contenido_de_cada_formato() throws Exception {
        when(consultar.consultar("foto.jpg"))
                .thenReturn(Optional.of(new ImagenAlmacenada(new byte[]{1, 2}, TipoImagen.JPEG)));

        mvc.perform(get("/api/imagenes/foto.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }

    @Test
    @DisplayName("una imagen que no existe responde 404")
    void una_imagen_inexistente_responde_404() throws Exception {
        when(consultar.consultar("no-existe.png")).thenReturn(Optional.empty());

        mvc.perform(get("/api/imagenes/no-existe.png"))
                .andExpect(status().isNotFound());
    }
}