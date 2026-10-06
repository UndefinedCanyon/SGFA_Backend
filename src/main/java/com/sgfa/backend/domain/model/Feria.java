package com.sgfa.backend.domain.model;

public class Feria {

    private Long id;
    private String nombreFeria;
    private Long idAdmin;
    private boolean activo;

    public Feria(Long id, String nombreFeria, Long idAdmin, boolean activo) {
        this.id = id;
        this.nombreFeria = nombreFeria;
        this.idAdmin = idAdmin;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombreFeria() {
        return nombreFeria;
    }

    public Long getIdAdmin() {
        return idAdmin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setNombreFeria(String nombreFeria) {
        this.nombreFeria = nombreFeria;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}