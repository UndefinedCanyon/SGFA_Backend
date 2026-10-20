package com.sgfa.backend.domain.model;

public class ImagenAlmacenada {

    private final byte[] contenido;
    private final TipoImagen tipo;

    public ImagenAlmacenada(byte[] contenido, TipoImagen tipo) {
        this.contenido = contenido;
        this.tipo = tipo;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public TipoImagen getTipo() {
        return tipo;
    }
}