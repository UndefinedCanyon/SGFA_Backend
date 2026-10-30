package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.AccesoNoAutorizadoException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.out.AlmacenImagenesPort;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductosServiceTest {

    private static final Long DUENO = 7L;
    private static final Long OTRO_ARTESANO = 8L;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;
    @Mock
    private AlmacenImagenesPort almacenImagenesPort;

    private Producto producto(String imagen) {
        return new Producto(5L, "Bolso", new BigDecimal("50000"), 3, DUENO, imagen);
    }

    @Nested
    @DisplayName("RegistrarProductoService")
    class Registrar {

        @Test
        @DisplayName("Crea el producto del artesano, sin imagen al inicio")
        void registra_producto() {
            RegistrarProductoService servicio = new RegistrarProductoService(productoRepositoryPort);
            when(productoRepositoryPort.guardar(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            servicio.registrar("Bolso", new BigDecimal("50000"), 3, DUENO);

            ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
            verify(productoRepositoryPort).guardar(captor.capture());
            Producto guardado = captor.getValue();

            assertNull(guardado.getId());
            assertEquals("Bolso", guardado.getNombre());
            assertEquals(new BigDecimal("50000"), guardado.getPrecio());
            assertEquals(3, guardado.getCantidad());
            assertEquals(DUENO, guardado.getIdArtesano());
            assertNull(guardado.getImagen());
        }

        @Test
        @DisplayName("Devuelve lo que entrega el repositorio")
        void devuelve_el_guardado() {
            RegistrarProductoService servicio = new RegistrarProductoService(productoRepositoryPort);
            Producto conId = producto(null);
            when(productoRepositoryPort.guardar(any(Producto.class))).thenReturn(conId);

            assertSame(conId, servicio.registrar("Bolso", new BigDecimal("50000"), 3, DUENO));
        }

        @Test
        @DisplayName("Guarda el nombre sin espacios sobrantes")
        void recorta_el_nombre() {
            RegistrarProductoService servicio = new RegistrarProductoService(productoRepositoryPort);
            when(productoRepositoryPort.guardar(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            Producto resultado = servicio.registrar("  Bolso  ", new BigDecimal("50000"), 3, DUENO);

            assertEquals("Bolso", resultado.getNombre());
        }

        @Test
        @DisplayName("Un producto con datos inválidos no se guarda")
        void datos_invalidos() {
            RegistrarProductoService servicio = new RegistrarProductoService(productoRepositoryPort);

            assertThrows(DatoInvalidoException.class, () -> servicio.registrar(" ", new BigDecimal("50000"), 3, DUENO));
            assertThrows(DatoInvalidoException.class, () -> servicio.registrar("Bolso", null, 3, DUENO));
            assertThrows(DatoInvalidoException.class, () -> servicio.registrar("Bolso", new BigDecimal("-1"), 3, DUENO));
            assertThrows(DatoInvalidoException.class, () -> servicio.registrar("Bolso", new BigDecimal("50000"), null, DUENO));
            assertThrows(DatoInvalidoException.class, () -> servicio.registrar("Bolso", new BigDecimal("50000"), -2, DUENO));

            verifyNoInteractions(productoRepositoryPort);
        }
    }

    @Nested
    @DisplayName("ActualizarProductoService")
    class Actualizar {

        private ActualizarProductoService servicio() {
            return new ActualizarProductoService(productoRepositoryPort);
        }

        @Test
        @DisplayName("El dueño actualiza nombre, precio y cantidad")
        void dueno_actualiza() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto("foto.jpg")));
            when(productoRepositoryPort.guardar(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

            Producto resultado = servicio().actualizar(5L, "Bolso grande", new BigDecimal("75000"), 10, DUENO);

            assertEquals("Bolso grande", resultado.getNombre());
            assertEquals(new BigDecimal("75000"), resultado.getPrecio());
            assertEquals(10, resultado.getCantidad());
            assertEquals("foto.jpg", resultado.getImagen());
        }

        @Test
        @DisplayName("Otro artesano no puede modificar el producto")
        void otro_artesano() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto(null)));

            assertThrows(AccesoNoAutorizadoException.class,
                    () -> servicio().actualizar(5L, "Robado", BigDecimal.ONE, 1, OTRO_ARTESANO));

            verify(productoRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Datos inválidos se rechazan sin modificar ni guardar el producto")
        void datos_invalidos() {
            Producto original = producto("foto.jpg");
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(original));

            assertThrows(DatoInvalidoException.class,
                    () -> servicio().actualizar(5L, "", new BigDecimal("75000"), 10, DUENO));
            assertThrows(DatoInvalidoException.class,
                    () -> servicio().actualizar(5L, "Bolso grande", BigDecimal.ZERO, 10, DUENO));
            assertThrows(DatoInvalidoException.class,
                    () -> servicio().actualizar(5L, "Bolso grande", new BigDecimal("75000"), null, DUENO));

            assertEquals("Bolso", original.getNombre());
            assertEquals(new BigDecimal("50000"), original.getPrecio());
            assertEquals(3, original.getCantidad());
            verify(productoRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Un artesano ajeno recibe la negativa de permiso antes que el error de datos")
        void permiso_antes_que_validacion() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto(null)));

            assertThrows(AccesoNoAutorizadoException.class,
                    () -> servicio().actualizar(5L, "", BigDecimal.ONE, 1, OTRO_ARTESANO));
        }

        @Test
        @DisplayName("Un producto que no existe se rechaza")
        void producto_inexistente() {
            when(productoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio().actualizar(99L, "X", BigDecimal.ONE, 1, DUENO));

            assertEquals("El producto no existe.", error.getMessage());
        }
    }

    @Nested
    @DisplayName("EliminarProductoService")
    class Eliminar {

        private EliminarProductoService servicio() {
            return new EliminarProductoService(productoRepositoryPort, almacenImagenesPort);
        }

        @Test
        @DisplayName("Elimina el producto y también su imagen guardada")
        void elimina_con_imagen() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto("foto.jpg")));

            servicio().eliminar(5L, DUENO);

            verify(productoRepositoryPort).eliminar(5L);
            verify(almacenImagenesPort).eliminar("foto.jpg");
        }

        @Test
        @DisplayName("Un producto sin imagen no toca el almacén de archivos")
        void elimina_sin_imagen() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto(null)));

            servicio().eliminar(5L, DUENO);

            verify(productoRepositoryPort).eliminar(5L);
            verifyNoInteractions(almacenImagenesPort);
        }

        @Test
        @DisplayName("Otro artesano no puede eliminar el producto")
        void otro_artesano() {
            when(productoRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(producto("foto.jpg")));

            assertThrows(AccesoNoAutorizadoException.class, () -> servicio().eliminar(5L, OTRO_ARTESANO));

            verify(productoRepositoryPort, never()).eliminar(anyLong());
            verify(almacenImagenesPort, never()).eliminar(anyString());
        }

        @Test
        @DisplayName("Un producto que no existe se rechaza")
        void producto_inexistente() {
            when(productoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio().eliminar(99L, DUENO));

            assertEquals("El producto no existe.", error.getMessage());
        }
    }

    @Nested
    @DisplayName("ConsultarProductosService")
    class Consultar {

        @Test
        @DisplayName("Devuelve los productos del artesano")
        void lista_por_artesano() {
            ConsultarProductosService servicio = new ConsultarProductosService(productoRepositoryPort);
            List<Producto> productos = List.of(producto(null));
            when(productoRepositoryPort.listarPorArtesano(DUENO)).thenReturn(productos);

            assertSame(productos, servicio.consultarPorArtesano(DUENO));
        }
    }
}