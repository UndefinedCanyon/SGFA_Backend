package com.sgfa.backend.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo que se le cuenta al asistente de cada solicitud pendiente. A propósito no incluye datos
 * personales (cédula, correo, teléfono, nombre de la persona): solo lo que describe el emprendimiento.
 */
public record ResumenSolicitud(Long idInscripcion,
                               String emprendimiento,
                               String descripcion,
                               List<String> productos,
                               LocalDate fechaInscripcion) {
}
