package com.sgfa.backend.infrastructure.adapter.out.ia;

import com.sgfa.backend.application.port.out.AsistenteIAPort;
import com.sgfa.backend.domain.model.ContextoEdicion;
import com.sgfa.backend.domain.model.PrioridadSolicitud;
import com.sgfa.backend.domain.model.ResultadoAsistente;
import com.sgfa.backend.domain.model.ResumenSolicitud;
import com.sgfa.backend.domain.model.SugerenciaSolicitud;

import java.util.List;

/**
 * Asistente de demostración que NO usa IA: aplica reglas simples y siempre da la misma respuesta.
 * Sirve para desarrollar y para las pruebas end to end sin internet ni cuota, y como respaldo cuando
 * no hay clave configurada. En pantalla aparece como «Demostración» para que no se confunda con IA real.
 */
public class AsistenteIAFalsoAdapter implements AsistenteIAPort {

    static final int DESCRIPCION_CLARA = 20;

    @Override
    public String proveedor() {
        return "Demostración";
    }

    @Override
    public ResultadoAsistente priorizarSolicitudes(ContextoEdicion contexto, List<ResumenSolicitud> solicitudes) {
        List<SugerenciaSolicitud> sugerencias = solicitudes.stream().map(this::sugerir).toList();

        String cupos = contexto.cuposDisponibles() == null
                ? "La edición no tiene límite de cupo."
                : "Quedan " + contexto.cuposDisponibles() + " cupos para " + contexto.totalPendientes() + " solicitudes pendientes.";
        String resumen = "Análisis de demostración (sin IA, basado en reglas simples). " + cupos;

        return new ResultadoAsistente(resumen, sugerencias);
    }

    private SugerenciaSolicitud sugerir(ResumenSolicitud solicitud) {
        int productos = solicitud.productos().size();
        boolean descripcionClara = solicitud.descripcion() != null && solicitud.descripcion().length() >= DESCRIPCION_CLARA;

        if (productos >= 2 && descripcionClara) {
            return new SugerenciaSolicitud(solicitud.idInscripcion(), PrioridadSolicitud.ALTA,
                    "Tiene " + productos + " productos registrados y una descripción clara.");
        }
        if (productos >= 1) {
            return new SugerenciaSolicitud(solicitud.idInscripcion(), PrioridadSolicitud.MEDIA,
                    descripcionClara ? "Tiene un solo producto registrado." : "Tiene productos, pero la descripción es corta o falta.");
        }
        return new SugerenciaSolicitud(solicitud.idInscripcion(), PrioridadSolicitud.BAJA,
                "Todavía no tiene productos registrados.");
    }
}
