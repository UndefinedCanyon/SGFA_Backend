package com.sgfa.backend.domain.model;

import java.time.LocalDate;

public class Inscripcion {

    private Long id;
    private Long idArtesano;
    private Long idEdicionFeria;
    private LocalDate fechaInscripcion;
    private String estado;

    public Inscripcion(Long id, Long idArtesano, Long idEdicionFeria, LocalDate fechaInscripcion, String estado) {
        this.id = id;
        this.idArtesano = idArtesano;
        this.idEdicionFeria = idEdicionFeria;
        this.fechaInscripcion = fechaInscripcion;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Long getIdArtesano() {
        return idArtesano;
    }

    public Long getIdEdicionFeria() {
        return idEdicionFeria;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}