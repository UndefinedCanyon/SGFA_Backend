package com.sgfa.backend.infrastructure.adapter.out.persistence;

import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoRepositoryAdapterTest extends PersistenciaTestBase {

    @Autowired
    private ProductoRepositoryAdapter productos;

    private Artesano ana;

    @BeforeEach
    void preparar() {
        ana = nuevoArtesano("ana@correo.com", "1010");
    }

    private Producto guardar(String nombre, String imagen) {
        return productos.guardar(new Producto(null, nombre, new BigDecimal("12000.50"), 3, ana.getId(), imagen));
    }

    @Test
    @DisplayName("guarda el producto y lo recupera con precio, cantidad e imagen")
    void guardar_y_buscar() {
        Producto guardado = guardar("Vasija", "abc.png");
        sincronizar();

        assertNotNull(guardado.getId());
        Producto leido = productos.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("Vasija", leido.getNombre());
        assertEquals(0, new BigDecimal("12000.50").compareTo(leido.getPrecio()));
        assertEquals(3, leido.getCantidad());
        assertEquals("abc.png", leido.getImagen());
        assertEquals(ana.getId(), leido.getIdArtesano());
    }

    @Test
    @DisplayName("un producto puede guardarse sin imagen")
    void sin_imagen() {
        Producto guardado = guardar("Taza", null);
        sincronizar();

        assertNull(productos.buscarPorId(guardado.getId()).orElseThrow().getImagen());
    }

    @Test
    @DisplayName("no guarda un producto de un artesano que no existe")
    void artesano_inexistente() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> productos.guardar(new Producto(null, "X", BigDecimal.ONE, 1, 9999L)));

        assertEquals("El artesano indicado no existe.", error.getMessage());
    }

    @Test
    @DisplayName("lista solo los productos del artesano pedido")
    void listar_por_artesano() {
        Artesano luis = nuevoArtesano("luis@correo.com", "2020");
        guardar("Vasija", null);
        guardar("Taza", null);
        productos.guardar(new Producto(null, "Plato", BigDecimal.TEN, 1, luis.getId()));

        assertEquals(2, productos.listarPorArtesano(ana.getId()).size());
        assertEquals(1, productos.listarPorArtesano(luis.getId()).size());
    }

    @Test
    @DisplayName("eliminar borra el producto")
    void eliminar() {
        Producto guardado = guardar("Vasija", null);

        productos.eliminar(guardado.getId());
        sincronizar();

        assertTrue(productos.buscarPorId(guardado.getId()).isEmpty());
    }
}