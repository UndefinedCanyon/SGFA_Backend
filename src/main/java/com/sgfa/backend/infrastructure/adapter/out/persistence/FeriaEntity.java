package com.sgfa.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "feria")
public class FeriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feria")
    private Long id;

    @Column(name = "nombre_feria", nullable = false)
    private String nombreFeria;

    @Column(name = "id_admin", nullable = false)
    private Long idAdmin;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public FeriaEntity() {
    }

    public FeriaEntity(Long id, String nombreFeria, Long idAdmin, boolean activo) {
        this.id = id;
        this.nombreFeria = nombreFeria;
        this.idAdmin = idAdmin;
        this.activo = activo;
    }

    public boolean isActivo() {
        return activo;
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
}