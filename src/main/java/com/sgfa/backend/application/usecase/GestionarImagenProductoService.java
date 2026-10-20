package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.AccesoNoAutorizadoException;
import com.sgfa.backend.application.port.in.GestionarImagenProductoUseCase;
import com.sgfa.backend.application.port.out.AlmacenImagenesPort;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.Producto;
import com.sgfa.backend.domain.model.TipoImagen;

public class GestionarImagenProductoService implements GestionarImagenProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;
    private final AlmacenImagenesPort almacenImagenesPort;

    public GestionarImagenProductoService(ProductoRepositoryPort productoRepositoryPort,
                                           AlmacenImagenesPort almacenImagenesPort) {
        this.productoRepositoryPort = productoRepositoryPort;
        this.almacenImagenesPort = almacenImagenesPort;
    }

    @Override
    public Producto subirImagen(Long idProducto, byte[] contenido, Long idArtesanoAutenticado) {
        Producto producto = buscarPropio(idProducto, idArtesanoAutenticado);
        TipoImagen tipo = ValidacionImagen.validar(contenido);

        String nueva = almacenImagenesPort.guardar(contenido, tipo.getExtension());
        String anterior = producto.getImagen();
        producto.setImagen(nueva);

        Producto guardado;
        try {
            guardado = productoRepositoryPort.guardar(producto);
        } catch (RuntimeException e) {
            almacenImagenesPort.eliminar(nueva);
            throw e;
        }

        if (anterior != null) {
            almacenImagenesPort.eliminar(anterior);
        }
        return guardado;
    }

    @Override
    public Producto quitarImagen(Long idProducto, Long idArtesanoAutenticado) {
        Producto producto = buscarPropio(idProducto, idArtesanoAutenticado);

        String anterior = producto.getImagen();
        if (anterior == null) {
            return producto;
        }

        producto.setImagen(null);
        Producto guardado = productoRepositoryPort.guardar(producto);
        almacenImagenesPort.eliminar(anterior);
        return guardado;
    }

    private Producto buscarPropio(Long idProducto, Long idArtesanoAutenticado) {
        Producto producto = productoRepositoryPort.buscarPorId(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe."));

        if (!producto.getIdArtesano().equals(idArtesanoAutenticado)) {
            throw new AccesoNoAutorizadoException("No tienes permiso para modificar este producto.");
        }
        return producto;
    }
}