package com.sgfa.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "precio", nullable = false)
    private BigDecimal precio;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @ManyToOne
    @JoinColumn(name = "id_artesano", nullable = false)
    private ArtesanoEntity artesano;

    public ProductoEntity() {
    }

    public ProductoEntity(Long id, String nombre, BigDecimal precio, Integer cantidad, ArtesanoEntity artesano) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.artesano = artesano;
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

    public ArtesanoEntity getArtesano() {
        return artesano;
    }
}