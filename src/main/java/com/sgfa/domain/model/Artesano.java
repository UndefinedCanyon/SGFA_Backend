package com.sgfa.backend.domain.model;

public class Artesano {

    private Long id;
    private String nombre;
    private String correoElectronico;
    private String contrasena;
    private String cc;
    private String telefono;
    private String nombreEmprendimiento;
    private String descripcionCorta;

    public Artesano(Long id, String nombre, String correoElectronico, String contrasena,
                     String cc, String telefono, String nombreEmprendimiento, String descripcionCorta) {
        this.id = id;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.contrasena = contrasena;
        this.cc = cc;
        this.telefono = telefono;
        this.nombreEmprendimiento = nombreEmprendimiento;
        this.descripcionCorta = descripcionCorta;
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

    public String getCc() {
        return cc;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getNombreEmprendimiento() {
        return nombreEmprendimiento;
    }

    public String getDescripcionCorta() {
        return descripcionCorta;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setNombreEmprendimiento(String nombreEmprendimiento) {
        this.nombreEmprendimiento = nombreEmprendimiento;
    }

    public void setDescripcionCorta(String descripcionCorta) {
        this.descripcionCorta = descripcionCorta;
    }
}