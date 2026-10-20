package com.sgfa.backend.domain.model;

import java.util.List;

public class ParticipantePublico {

    private Long id;
    private String nombreEmprendimiento;
    private String descripcionCorta;
    private String logo;
    private List<Producto> productos;

    public ParticipantePublico(Long id, String nombreEmprendimiento, String descripcionCorta,
                                String logo, List<Producto> productos) {
        this.id = id;
        this.nombreEmprendimiento = nombreEmprendimiento;
        this.descripcionCorta = descripcionCorta;
        this.logo = logo;
        this.productos = productos;
    }

    public Long getId() {
        return id;
    }

    public String getNombreEmprendimiento() {
        return nombreEmprendimiento;
    }

    public String getDescripcionCorta() {
        return descripcionCorta;
    }

    public String getLogo() {
        return logo;
    }

    public List<Producto> getProductos() {
        return productos;
    }
}