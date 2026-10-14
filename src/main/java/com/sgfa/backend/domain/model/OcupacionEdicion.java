package com.sgfa.backend.domain.model;

public class OcupacionEdicion {

    private final Integer cupoMaximo;
    private final long aprobados;

    public OcupacionEdicion(Integer cupoMaximo, long aprobados) {
        this.cupoMaximo = cupoMaximo;
        this.aprobados = aprobados;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public long getAprobados() {
        return aprobados;
    }

    public Integer getDisponibles() {
        if (cupoMaximo == null) {
            return null;
        }
        return (int) Math.max(0, cupoMaximo - aprobados);
    }
}