package com.sgfa.backend.domain.model;

public class Lugar {

    private Long id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;

    public Lugar(Long id, String nombre, String direccion, Double latitud, Double longitud) {
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

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setUbicacion(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }
}