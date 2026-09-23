package com.sgfa.backend.domain.model;

public class Administrador {

    private Long id;
    private String nombre;
    private String correoElectronico;
    private String contrasena;

    public Administrador(Long id, String nombre, String correoElectronico, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.contrasena = contrasena;
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

    public String getContrasena() {
        return contrasena;
    }
}