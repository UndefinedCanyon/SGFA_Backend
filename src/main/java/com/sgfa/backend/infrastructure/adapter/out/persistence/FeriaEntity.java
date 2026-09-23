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

    public FeriaEntity() {
    }

    public FeriaEntity(Long id, String nombreFeria, Long idAdmin) {
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
}