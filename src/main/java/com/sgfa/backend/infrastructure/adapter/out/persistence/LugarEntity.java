package com.sgfa.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "lugar")
public class LugarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lugar")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "direccion", nullable = false)
    private String direccion;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    // JPA exige un constructor vacío
    public LugarEntity() {
    }

    public LugarEntity(Long id, String nombre, String direccion, Double latitud, Double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }
}