package com.sgfa.backend.domain.model;

import java.math.BigDecimal;

public class Producto {

    private Long id;
    private String nombre;
    private BigDecimal precio;
    private Integer cantidad;
    private Long idArtesano;

    public Producto(Long id, String nombre, BigDecimal precio, Integer cantidad, Long idArtesano) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.idArtesano = idArtesano;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public Long getIdArtesano() {
        return idArtesano;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}