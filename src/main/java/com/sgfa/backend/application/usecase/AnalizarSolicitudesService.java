package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.in.AnalizarSolicitudesUseCase;
import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.AsistenteIAPort;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.FeriaRepositoryPort;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.application.port.out.ProductoRepositoryPort;
import com.sgfa.backend.domain.model.AnalisisSolicitudes;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.ContextoEdicion;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Feria;
import com.sgfa.backend.domain.model.Inscripcion;
import com.sgfa.backend.domain.model.Lugar;
import com.sgfa.backend.domain.model.OcupacionEdicion;
import com.sgfa.backend.domain.model.PrioridadSolicitud;
import com.sgfa.backend.domain.model.Producto;
import com.sgfa.backend.domain.model.ResultadoAsistente;
import com.sgfa.backend.domain.model.ResumenSolicitud;
import com.sgfa.backend.domain.model.SugerenciaSolicitud;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AnalizarSolicitudesService implements AnalizarSolicitudesUseCase {

    /** Tope de solicitudes por análisis: mantiene corto lo que se envía y respeta la cuota gratuita. */
    static final int MAX_SOLICITUDES = 15;
    static final int MAX_PRODUCTOS = 8;
    static final int MAX_TEXTO_DESCRIPCION = 300;
    static final int MAX_TEXTO_NOMBRE = 100;
    static final String SIN_SUGERENCIA = "El asistente no dio una sugerencia para esta solicitud.";

    private static final String PENDIENTE = "PENDIENTE";

    private final InscripcionRepositoryPort inscripcionRepositoryPort;
    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;
    private final FeriaRepositoryPort feriaRepositoryPort;
    private final LugarRepositoryPort lugarRepositoryPort;
    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final AsistenteIAPort asistenteIAPort;

    public AnalizarSolicitudesService(InscripcionRepositoryPort inscripcionRepositoryPort,
                                      EdicionFeriaRepositoryPort edicionFeriaRepositoryPort,
                                      FeriaRepositoryPort feriaRepositoryPort,
                                      LugarRepositoryPort lugarRepositoryPort,
                                      ArtesanoRepositoryPort artesanoRepositoryPort,
                                      ProductoRepositoryPort productoRepositoryPort,
                                      AsistenteIAPort asistenteIAPort) {
        this.inscripcionRepositoryPort = inscripcionRepositoryPort;
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
        this.feriaRepositoryPort = feriaRepositoryPort;
        this.lugarRepositoryPort = lugarRepositoryPort;
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.asistenteIAPort = asistenteIAPort;
    }

    @Override
    public AnalisisSolicitudes analizar(Long idEdicionFeria) {
        if (idEdicionFeria == null) {
            throw new DatoInvalidoException("Indica la edición que quieres analizar.");
        }

        EdicionFeria edicion = edicionFeriaRepositoryPort.buscarPorId(idEdicionFeria)
                .orElseThrow(() -> new IllegalArgumentException("La edición no existe."));

        OcupacionEdicion ocupacion = new OcupacionEdicion(
                edicion.getCupoMaximo(), inscripcionRepositoryPort.contarAprobadasPorEdicion(idEdicionFeria));

        List<Inscripcion> pendientes = inscripcionRepositoryPort.listarTodas().stream()
                .filter(i -> idEdicionFeria.equals(i.getIdEdicionFeria()) && PENDIENTE.equals(i.getEstado()))
                .sorted(Comparator.comparing(Inscripcion::getFechaInscripcion,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<ResumenSolicitud> resumenes = new ArrayList<>();
        for (Inscripcion inscripcion : pendientes) {
            if (resumenes.size() >= MAX_SOLICITUDES) {
                break;
            }
            resumir(inscripcion).ifPresent(resumenes::add);
        }

        // Sin nada que analizar no se llama a la IA: se ahorra cuota y tiempo.
        if (resumenes.isEmpty()) {
            return new AnalisisSolicitudes(idEdicionFeria, "No hay solicitudes pendientes en esta edición.",
                    ocupacion.getDisponibles(), pendientes.size(), 0, asistenteIAPort.proveedor(), List.of());
        }

        ContextoEdicion contexto = new ContextoEdicion(
                nombreFeria(edicion), nombreLugar(edicion), edicion.getFechaInicio(), edicion.getFechaFin(),
                edicion.getCupoMaximo(), ocupacion.getAprobados(), ocupacion.getDisponibles(), pendientes.size());

        ResultadoAsistente resultado = asistenteIAPort.priorizarSolicitudes(contexto, resumenes);

        return new AnalisisSolicitudes(idEdicionFeria, resultado.resumen(), ocupacion.getDisponibles(),
                pendientes.size(), resumenes.size(), asistenteIAPort.proveedor(),
                completarSugerencias(resumenes, resultado.sugerencias()));
    }

    private Optional<ResumenSolicitud> resumir(Inscripcion inscripcion) {
        Optional<Artesano> artesano = artesanoRepositoryPort.buscarPorId(inscripcion.getIdArtesano());
        if (artesano.isEmpty()) {
            return Optional.empty();
        }

        List<String> productos = productoRepositoryPort.listarPorArtesano(inscripcion.getIdArtesano()).stream()
                .map(Producto::getNombre)
                .map(nombre -> limpiar(nombre, MAX_TEXTO_NOMBRE))
                .limit(MAX_PRODUCTOS)
                .toList();

        return Optional.of(new ResumenSolicitud(
                inscripcion.getId(),
                limpiar(artesano.get().getNombreEmprendimiento(), MAX_TEXTO_NOMBRE),
                limpiar(artesano.get().getDescripcionCorta(), MAX_TEXTO_DESCRIPCION),
                productos,
                inscripcion.getFechaInscripcion()));
    }

    /**
     * Se queda solo con sugerencias de solicitudes que de verdad se analizaron (la IA puede inventar ids),
     * agrega una por defecto a las que faltaron y deja primero las de prioridad alta.
     */
    private List<SugerenciaSolicitud> completarSugerencias(List<ResumenSolicitud> resumenes,
                                                           List<SugerenciaSolicitud> recibidas) {
        Map<Long, SugerenciaSolicitud> porId = new LinkedHashMap<>();
        for (ResumenSolicitud resumen : resumenes) {
            porId.put(resumen.idInscripcion(), null);
        }
        if (recibidas != null) {
            for (SugerenciaSolicitud sugerencia : recibidas) {
                if (sugerencia != null && porId.containsKey(sugerencia.idInscripcion())
                        && porId.get(sugerencia.idInscripcion()) == null) {
                    porId.put(sugerencia.idInscripcion(), sugerencia);
                }
            }
        }

        List<SugerenciaSolicitud> completas = new ArrayList<>();
        porId.forEach((id, sugerencia) -> completas.add(sugerencia != null
                ? sugerencia
                : new SugerenciaSolicitud(id, PrioridadSolicitud.MEDIA, SIN_SUGERENCIA)));

        completas.sort(Comparator.comparing(s -> s.prioridad() == null ? PrioridadSolicitud.MEDIA : s.prioridad()));
        return completas;
    }

    private String nombreFeria(EdicionFeria edicion) {
        return feriaRepositoryPort.buscarPorId(edicion.getIdFeria())
                .map(Feria::getNombreFeria)
                .orElse("Feria #" + edicion.getIdFeria());
    }

    private String nombreLugar(EdicionFeria edicion) {
        return lugarRepositoryPort.buscarPorId(edicion.getIdLugar())
                .map(Lugar::getNombre)
                .orElse("Lugar #" + edicion.getIdLugar());
    }

    /** Quita saltos de línea y espacios repetidos (para que el texto no rompa el formato) y lo acorta. */
    static String limpiar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.replaceAll("\\s+", " ").trim();
        return limpio.length() <= maximo ? limpio : limpio.substring(0, maximo).trim() + "…";
    }
}
