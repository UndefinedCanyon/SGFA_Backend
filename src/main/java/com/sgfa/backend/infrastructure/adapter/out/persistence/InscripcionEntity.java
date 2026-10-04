package com.sgfa.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "inscripcion")
public class InscripcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_artesano", nullable = false)
    private ArtesanoEntity artesano;

    @ManyToOne
    @JoinColumn(name = "id_edicionferia", nullable = false)
    private EdicionFeriaEntity edicionFeria;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDate fechaInscripcion;

    @Column(name = "estado", nullable = false)
    private String estado;

    public InscripcionEntity() {
    }

    public InscripcionEntity(Long id, ArtesanoEntity artesano, EdicionFeriaEntity edicionFeria,
                              LocalDate fechaInscripcion, String estado) {
        this.id = id;
        this.artesano = artesano;
        this.edicionFeria = edicionFeria;
        this.fechaInscripcion = fechaInscripcion;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public ArtesanoEntity getArtesano() {
        return artesano;
    }

    public EdicionFeriaEntity getEdicionFeria() {
        return edicionFeria;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public String getEstado() {
        return estado;
    }
}