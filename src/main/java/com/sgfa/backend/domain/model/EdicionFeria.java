package com.sgfa.backend.domain.model;

import java.time.LocalDate;

public class EdicionFeria {

    private Long id;
    private Long idFeria;
    private Long idLugar;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activo;
    private Integer cupoMaximo;

    public EdicionFeria(Long id, Long idFeria, Long idLugar, LocalDate fechaInicio, LocalDate fechaFin,
                        boolean activo, Integer cupoMaximo) {
        this.id = id;
        this.idFeria = idFeria;
        this.idLugar = idLugar;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = activo;
        this.cupoMaximo = cupoMaximo;
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

    public boolean isActivo() {
        return activo;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }
}