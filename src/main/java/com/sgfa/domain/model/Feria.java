package com.sgfa.backend.domain.model;

public class Feria {

    private Long id;
    private String nombreFeria;
    private Long idAdmin;

    public Feria(Long id, String nombreFeria, Long idAdmin) {
        this.id = id;
        this.nombreFeria = nombreFeria;
        this.idAdmin = idAdmin;
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

    public void setNombreFeria(String nombreFeria) {
        this.nombreFeria = nombreFeria;
    }
}