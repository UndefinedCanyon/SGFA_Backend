package com.sgfa.backend.domain.model;

import java.time.LocalDate;

/** Datos de la edición que ayudan al asistente a ponderar las solicitudes (sobre todo el cupo). */
public record ContextoEdicion(String nombreFeria,
                              String lugar,
                              LocalDate fechaInicio,
                              LocalDate fechaFin,
                              Integer cupoMaximo,
                              long aprobados,
                              Integer cuposDisponibles,
                              int totalPendientes) {
}
