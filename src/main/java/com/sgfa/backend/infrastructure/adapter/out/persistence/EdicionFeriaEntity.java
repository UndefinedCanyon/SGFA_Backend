package com.sgfa.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "edicion_feria")
public class EdicionFeriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_edicionferia")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_feria", nullable = false)
    private FeriaEntity feria;

    @ManyToOne
    @JoinColumn(name = "id_lugar", nullable = false)
    private LugarEntity lugar;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    public EdicionFeriaEntity() {
    }

    public EdicionFeriaEntity(Long id, FeriaEntity feria, LugarEntity lugar,
                               LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.feria = feria;
        this.lugar = lugar;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public Long getId() {
        return id;
    }

    public FeriaEntity getFeria() {
        return feria;
    }

    public LugarEntity getLugar() {
        return lugar;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
}