package com.sgfa.backend.domain.model;

public class SesionIniciada {

    private Long id;
    private String nombre;
    private String correoElectronico;
    private String rol; // "ADMINISTRADOR" o "ARTESANO"

    public SesionIniciada(Long id, String nombre, String correoElectronico, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public String getRol() {
        return rol;
    }
}