package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.AccesoNoAutorizadoException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.out.AlmacenImagenesPort;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.ImagenAlmacenada;
import com.sgfa.backend.domain.model.Producto;
import com.sgfa.backend.domain.model.TipoImagen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImagenesServiceTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};
    private static final Long DUENO = 7L;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;
    @Mock
    private ArtesanoRepositoryPort artesanoRepositoryPort;
    @Mock
    private AlmacenImagenesPort almacenImagenesPort;

    private Producto producto(String imagen) {
        return new Producto(5L, "Bolso", new BigDecimal("50000"), 3, DUENO, imagen);
    }

    private Artesano artesano(String logo) {
        return new Artesano(DUENO, "María", "maria@correo.com", "hash", "123456789", null,
                "Tejidos", null, true, logo, true);
    }

    @Nested
    @DisplayName("GestionarImagenProductoService")
    class ImagenProducto {

        private GestionarImagenProductoService servicio() {
            return new GestionarImagenProductoService(productoRepositoryPort, almacenImagenesPort);
        }

        @Test
        @DisplayName("Sube la imagen nueva y borra la anterior después de guardar el producto")
        void reemplaza_la_imagen() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto("vieja.jpg")));
            when(almacenImagenesPort.guardar(any(byte[].class), eq("jpg"))).thenReturn("nueva.jpg");
            when(productoRepositoryPort.guardar(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            Producto resultado = servicio().subirImagen(5L, JPEG, DUENO);

            assertEquals("nueva.jpg", resultado.getImagen());
            InOrder orden = inOrder(productoRepositoryPort, almacenImagenesPort);
            orden.verify(productoRepositoryPort).guardar(any(Producto.class));
            orden.verify(almacenImagenesPort).eliminar("vieja.jpg");
        }

        @Test
        @DisplayName("Sin imagen anterior no se borra nada")
        void primera_imagen() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto(null)));
            when(almacenImagenesPort.guardar(any(byte[].class), eq("png"))).thenReturn("nueva.png");
            when(productoRepositoryPort.guardar(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            Producto resultado = servicio().subirImagen(5L, PNG, DUENO);

            assertEquals("nueva.png", resultado.getImagen());
            verify(almacenImagenesPort, never()).eliminar(anyString());
        }

        @Test
        @DisplayName("Si falla al guardar el producto, se borra la imagen nueva y se conserva la anterior")
        void deshace_si_falla_el_guardado() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto("vieja.jpg")));
            when(almacenImagenesPort.guardar(any(byte[].class), eq("jpg"))).thenReturn("nueva.jpg");
            when(productoRepositoryPort.guardar(any(Producto.class)))
                    .thenThrow(new IllegalStateException("fallo de base de datos"));

            assertThrows(IllegalStateException.class, () -> servicio().subirImagen(5L, JPEG, DUENO));

            verify(almacenImagenesPort).eliminar("nueva.jpg");
            verify(almacenImagenesPort, never()).eliminar("vieja.jpg");
        }

        @Test
        @DisplayName("Un archivo que no es una imagen permitida se rechaza sin guardarlo")
        void formato_no_permitido() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto(null)));

            byte[] texto = "esto no es una imagen".getBytes();
            assertThrows(DatoInvalidoException.class, () -> servicio().subirImagen(5L, texto, DUENO));

            verifyNoInteractions(almacenImagenesPort);
            verify(productoRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Otro artesano no puede cambiar la imagen del producto")
        void otro_artesano() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto(null)));

            assertThrows(AccesoNoAutorizadoException.class, () -> servicio().subirImagen(5L, JPEG, 8L));
            assertThrows(AccesoNoAutorizadoException.class, () -> servicio().quitarImagen(5L, 8L));

            verifyNoInteractions(almacenImagenesPort);
        }

        @Test
        @DisplayName("Un producto que no existe se rechaza")
        void producto_inexistente() {
            when(productoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio().subirImagen(99L, JPEG, DUENO));

            assertEquals("El producto no existe.", error.getMessage());
        }

        @Test
        @DisplayName("Quita la imagen del producto y borra el archivo")
        void quita_la_imagen() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto("vieja.jpg")));
            when(productoRepositoryPort.guardar(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            Producto resultado = servicio().quitarImagen(5L, DUENO);

            assertNull(resultado.getImagen());
            verify(almacenImagenesPort).eliminar("vieja.jpg");
        }

        @Test
        @DisplayName("Quitar la imagen de un producto que no tiene no hace nada")
        void quitar_sin_imagen() {
            Producto sinImagen = producto(null);
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(sinImagen));

            assertSame(sinImagen, servicio().quitarImagen(5L, DUENO));

            verify(productoRepositoryPort, never()).guardar(any());
            verifyNoInteractions(almacenImagenesPort);
        }
    }

    @Nested
    @DisplayName("GestionarLogoArtesanoService")
    class Logo {

        private GestionarLogoArtesanoService servicio() {
            return new GestionarLogoArtesanoService(artesanoRepositoryPort, almacenImagenesPort);
        }

        @Test
        @DisplayName("Sube el logo nuevo y borra el anterior")
        void reemplaza_el_logo() {
            when(artesanoRepositoryPort.buscarPorId(DUENO)).thenReturn(Optional.of(artesano("logo-viejo.png")));
            when(almacenImagenesPort.guardar(any(byte[].class), eq("png"))).thenReturn("logo-nuevo.png");
            when(artesanoRepositoryPort.guardar(any(Artesano.class))).thenAnswer(inv -> inv.getArgument(0));

            Artesano resultado = servicio().subirLogo(DUENO, PNG);

            assertEquals("logo-nuevo.png", resultado.getLogo());
            verify(almacenImagenesPort).eliminar("logo-viejo.png");
        }

        @Test
        @DisplayName("Si falla al guardar el artesano, se borra el logo nuevo y se conserva el anterior")
        void deshace_si_falla_el_guardado() {
            when(artesanoRepositoryPort.buscarPorId(DUENO)).thenReturn(Optional.of(artesano("logo-viejo.png")));
            when(almacenImagenesPort.guardar(any(byte[].class), eq("png"))).thenReturn("logo-nuevo.png");
            when(artesanoRepositoryPort.guardar(any(Artesano.class)))
                    .thenThrow(new IllegalStateException("fallo de base de datos"));

            assertThrows(IllegalStateException.class, () -> servicio().subirLogo(DUENO, PNG));

            verify(almacenImagenesPort).eliminar("logo-nuevo.png");
            verify(almacenImagenesPort, never()).eliminar("logo-viejo.png");
        }

        @Test
        @DisplayName("Un archivo vacío se rechaza")
        void archivo_vacio() {
            when(artesanoRepositoryPort.buscarPorId(DUENO)).thenReturn(Optional.of(artesano(null)));

            assertThrows(DatoInvalidoException.class, () -> servicio().subirLogo(DUENO, new byte[0]));

            verifyNoInteractions(almacenImagenesPort);
        }

        @Test
        @DisplayName("Quita el logo y borra el archivo")
        void quita_el_logo() {
            when(artesanoRepositoryPort.buscarPorId(DUENO)).thenReturn(Optional.of(artesano("logo.png")));
            when(artesanoRepositoryPort.guardar(any(Artesano.class))).thenAnswer(inv -> inv.getArgument(0));

            Artesano resultado = servicio().quitarLogo(DUENO);

            assertNull(resultado.getLogo());
            verify(almacenImagenesPort).eliminar("logo.png");
        }

        @Test
        @DisplayName("Quitar el logo cuando no hay ninguno no hace nada")
        void quitar_sin_logo() {
            Artesano sinLogo = artesano(null);
            when(artesanoRepositoryPort.buscarPorId(DUENO)).thenReturn(Optional.of(sinLogo));

            assertSame(sinLogo, servicio().quitarLogo(DUENO));

            verify(artesanoRepositoryPort, never()).guardar(any());
            verifyNoInteractions(almacenImagenesPort);
        }

        @Test
        @DisplayName("Un artesano que no existe se rechaza")
        void artesano_inexistente() {
            when(artesanoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio().subirLogo(99L, PNG));

            assertEquals("El artesano no existe.", error.getMessage());
        }
    }

    @Nested
    @DisplayName("ConsultarImagenService")
    class Consultar {

        private ConsultarImagenService servicio() {
            return new ConsultarImagenService(almacenImagenesPort);
        }

        @Test
        @DisplayName("Devuelve el contenido y el tipo según la extensión del nombre")
        void devuelve_la_imagen() {
            when(almacenImagenesPort.cargar("abc123.png")).thenReturn(Optional.of(PNG));

            Optional<ImagenAlmacenada> resultado = servicio().consultar("abc123.png");

            assertTrue(resultado.isPresent());
            assertEquals(TipoImagen.PNG, resultado.get().getTipo());
            assertArrayEquals(PNG, resultado.get().getContenido());
        }

        @Test
        @DisplayName("La extensión se reconoce sin importar mayúsculas")
        void extension_en_mayusculas() {
            when(almacenImagenesPort.cargar("foto.JPG")).thenReturn(Optional.of(JPEG));

            assertEquals(TipoImagen.JPEG, servicio().consultar("foto.JPG").orElseThrow().getTipo());
        }

        @Test
        @DisplayName("Un nombre sin extensión no se busca en el almacén")
        void sin_extension() {
            assertTrue(servicio().consultar("sinextension").isEmpty());

            verifyNoInteractions(almacenImagenesPort);
        }

        @Test
        @DisplayName("Una extensión no permitida no se busca en el almacén")
        void extension_no_permitida() {
            assertTrue(servicio().consultar("script.exe").isEmpty());
            assertTrue(servicio().consultar("animacion.gif").isEmpty());

            verifyNoInteractions(almacenImagenesPort);
        }

        @Test
        @DisplayName("Si el archivo no está en el almacén, no hay imagen")
        void archivo_inexistente() {
            when(almacenImagenesPort.cargar("fantasma.png")).thenReturn(Optional.empty());

            assertTrue(servicio().consultar("fantasma.png").isEmpty());
        }
    }
}