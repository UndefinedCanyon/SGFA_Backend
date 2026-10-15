package com.sgfa.backend.domain.model;

import java.time.LocalDate;

public class EdicionFeria {

    private Long id;
    private Long idFeria;
    private Long idLugar;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public EdicionFeria(Long id, Long idFeria, Long idLugar, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.idFeria = idFeria;
        this.idLugar = idLugar;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Long getId() {
        return id;
    }

    public Long getIdFeria() {
        return idFeria;
    }

    public Long getIdLugar() {
        return idLugar;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
}