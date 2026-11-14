package com.sgfa.backend.domain.model;

import java.util.List;

/**
 * Resultado que se le muestra al administrador. {@code proveedor} dice quién generó el análisis
 * (por ejemplo "Gemini" o "Demostración"), para que nunca se confunda con una decisión del sistema.
 */
public record AnalisisSolicitudes(Long idEdicionFeria,
                                  String resumen,
                                  Integer cuposDisponibles,
                                  int totalPendientes,
                                  int analizadas,
                                  String proveedor,
                                  List<SugerenciaSolicitud> sugerencias) {
}
