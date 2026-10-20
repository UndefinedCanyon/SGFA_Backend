package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.port.out.ArtesanoRepositoryPort;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.EnviadorCorreoPort;
import com.sgfa.backend.application.port.out.FeriaRepositoryPort;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.domain.model.Artesano;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Feria;
import com.sgfa.backend.domain.model.Inscripcion;
import com.sgfa.backend.domain.model.Lugar;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificadorSolicitud {

    private static final Logger LOG = Logger.getLogger(NotificadorSolicitud.class.getName());
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ArtesanoRepositoryPort artesanoRepositoryPort;
    private final EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;
    private final FeriaRepositoryPort feriaRepositoryPort;
    private final LugarRepositoryPort lugarRepositoryPort;
    private final EnviadorCorreoPort enviadorCorreoPort;
    private final String urlApp;

    public NotificadorSolicitud(ArtesanoRepositoryPort artesanoRepositoryPort,
                                 EdicionFeriaRepositoryPort edicionFeriaRepositoryPort,
                                 FeriaRepositoryPort feriaRepositoryPort,
                                 LugarRepositoryPort lugarRepositoryPort,
                                 EnviadorCorreoPort enviadorCorreoPort,
                                 String urlApp) {
        this.artesanoRepositoryPort = artesanoRepositoryPort;
        this.edicionFeriaRepositoryPort = edicionFeriaRepositoryPort;
        this.feriaRepositoryPort = feriaRepositoryPort;
        this.lugarRepositoryPort = lugarRepositoryPort;
        this.enviadorCorreoPort = enviadorCorreoPort;
        this.urlApp = urlApp.endsWith("/") ? urlApp.substring(0, urlApp.length() - 1) : urlApp;
    }

    public void avisarAprobada(Inscripcion inscripcion) {
        avisar(inscripcion, true, false);
    }

    /** eraAprobada: true si la participación ya estaba aprobada y ahora se cancela. */
    public void avisarRechazada(Inscripcion inscripcion, boolean eraAprobada) {
        avisar(inscripcion, false, eraAprobada);
    }

    private void avisar(Inscripcion inscripcion, boolean aprobada, boolean eraAprobada) {
        try {
            Optional<Artesano> artesano = artesanoRepositoryPort.buscarPorId(inscripcion.getIdArtesano())
                    .filter(a -> a.isActivo() && a.isCorreoVerificado());
            Optional<EdicionFeria> edicion = edicionFeriaRepositoryPort.buscarPorId(inscripcion.getIdEdicionFeria());

            if (artesano.isEmpty() || edicion.isEmpty()) {
                return;
            }

            String nombreFeria = feriaRepositoryPort.buscarPorId(edicion.get().getIdFeria())
                    .map(Feria::getNombreFeria)
                    .orElse("la feria");
            String nombreLugar = lugarRepositoryPort.buscarPorId(edicion.get().getIdLugar())
                    .map(Lugar::getNombre)
                    .orElse("el lugar por confirmar");
            String fechas = edicion.get().getFechaInicio().format(FORMATO_FECHA)
                    + " al " + edicion.get().getFechaFin().format(FORMATO_FECHA);
            String detalle = "«" + nombreFeria + "», del " + fechas + ", en " + nombreLugar;

            String asunto;
            String titulo;
            String texto;
            String textoBoton;
            String ruta;
            String nota;

            if (aprobada) {
                asunto = "Tu solicitud fue aprobada en SGFA";
                titulo = "¡Tu solicitud fue aprobada!";
                texto = "Participas en " + detalle
                        + ". Ya apareces como participante en la vista pública de esa edición.";
                textoBoton = "Ver mi panel";
                ruta = "/mi-panel";
                nota = "Revisa que tu perfil y tus productos estén al día: es lo que verá el público.";
            } else if (eraAprobada) {
                asunto = "Tu participación fue cancelada en SGFA";
                titulo = "Tu participación fue cancelada";
                texto = "Tu participación en " + detalle
                        + " ya no está confirmada, y dejarás de aparecer como participante de esa edición.";
                textoBoton = "Ver ferias";
                ruta = "/ferias";
                nota = "Si crees que es un error, comunícate con la organización de la feria.";
            } else {
                asunto = "Actualización de tu solicitud en SGFA";
                titulo = "Tu solicitud no fue aprobada";
                texto = "El administrador no aprobó tu solicitud para " + detalle
                        + ". Puedes solicitar participar en otras ediciones.";
                textoBoton = "Ver ferias";
                ruta = "/ferias";
                nota = "Si tienes dudas, comunícate con la organización de la feria.";
            }

            String cuerpo = PlantillaCorreo.construir(
                    artesano.get().getNombre(), titulo, texto, textoBoton, urlApp + ruta, nota);

            enviadorCorreoPort.enviar(artesano.get().getCorreoElectronico(), asunto, cuerpo);
        } catch (RuntimeException e) {
            // El aviso nunca debe impedir que la solicitud se apruebe o se rechace.
            LOG.log(Level.WARNING, "No se pudo preparar el aviso de la solicitud " + inscripcion.getId(), e);
        }
    }
}