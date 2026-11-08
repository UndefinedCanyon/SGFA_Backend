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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificadorSolicitudTest {

    private ArtesanoRepositoryPort artesanos;
    private EdicionFeriaRepositoryPort ediciones;
    private FeriaRepositoryPort ferias;
    private LugarRepositoryPort lugares;
    private EnviadorCorreoPort correo;
    private NotificadorSolicitud notificador;
    private final Inscripcion inscripcion = new Inscripcion(1L, 7L, 4L, LocalDate.of(2026, 11, 1), "PENDIENTE");

    private static Artesano artesano(boolean activo, boolean verificado) {
        return new Artesano(7L, "Ana", "ana@correo.com", "hash", "1010", "300",
                "Cerámicas Ana", "Vasijas", activo, null, verificado);
    }

    @BeforeEach
    void preparar() {
        artesanos = mock(ArtesanoRepositoryPort.class);
        ediciones = mock(EdicionFeriaRepositoryPort.class);
        ferias = mock(FeriaRepositoryPort.class);
        lugares = mock(LugarRepositoryPort.class);
        correo = mock(EnviadorCorreoPort.class);
        notificador = new NotificadorSolicitud(artesanos, ediciones, ferias, lugares, correo, "https://app.test/");

        when(artesanos.buscarPorId(7L)).thenReturn(Optional.of(artesano(true, true)));
        when(ediciones.buscarPorId(4L)).thenReturn(Optional.of(
                new EdicionFeria(4L, 1L, 2L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3), true, null)));
        when(ferias.buscarPorId(1L)).thenReturn(Optional.of(new Feria(1L, "Feria Andina", 1L, true)));
        when(lugares.buscarPorId(2L)).thenReturn(Optional.of(new Lugar(2L, "Plaza Mayor", "Calle 1", 4.0, -74.0)));
    }

    private ArgumentCaptor<String> enviarYCapturarCuerpo(String asuntoEsperado) {
        ArgumentCaptor<String> cuerpo = ArgumentCaptor.forClass(String.class);
        verify(correo).enviar(org.mockito.ArgumentMatchers.eq("ana@correo.com"),
                org.mockito.ArgumentMatchers.eq(asuntoEsperado), cuerpo.capture());
        return cuerpo;
    }

    @Test
    @DisplayName("al aprobar envía el correo con la feria, las fechas, el lugar y el enlace al panel")
    void aviso_de_aprobacion() {
        notificador.avisarAprobada(inscripcion);

        String cuerpo = enviarYCapturarCuerpo("Tu solicitud fue aprobada en SGFA").getValue();
        assertTrue(cuerpo.contains("Feria Andina"));
        assertTrue(cuerpo.contains("01/12/2026 al 03/12/2026"));
        assertTrue(cuerpo.contains("Plaza Mayor"));
        assertTrue(cuerpo.contains("https://app.test/mi-panel"));
        assertTrue(cuerpo.contains("Ana"));
    }

    @Test
    @DisplayName("al rechazar una solicitud pendiente envía el aviso de no aprobada")
    void aviso_de_rechazo() {
        notificador.avisarRechazada(inscripcion, false);

        String cuerpo = enviarYCapturarCuerpo("Actualización de tu solicitud en SGFA").getValue();
        assertTrue(cuerpo.contains("https://app.test/ferias"));
    }

    @Test
    @DisplayName("al rechazar una participación ya aprobada envía el aviso de cancelación")
    void aviso_de_cancelacion() {
        notificador.avisarRechazada(inscripcion, true);

        String cuerpo = enviarYCapturarCuerpo("Tu participación fue cancelada en SGFA").getValue();
        assertTrue(cuerpo.contains("https://app.test/ferias"));
    }

    @Test
    @DisplayName("la URL base con barra final no duplica la barra")
    void url_sin_doble_barra() {
        notificador.avisarAprobada(inscripcion);

        assertTrue(!enviarYCapturarCuerpo("Tu solicitud fue aprobada en SGFA").getValue().contains("app.test//"));
    }

    @Test
    @DisplayName("si la feria o el lugar ya no existen usa textos por defecto")
    void textos_por_defecto() {
        when(ferias.buscarPorId(1L)).thenReturn(Optional.empty());
        when(lugares.buscarPorId(2L)).thenReturn(Optional.empty());

        notificador.avisarAprobada(inscripcion);

        String cuerpo = enviarYCapturarCuerpo("Tu solicitud fue aprobada en SGFA").getValue();
        assertTrue(cuerpo.contains("la feria"));
        assertTrue(cuerpo.contains("el lugar por confirmar"));
    }

    @Test
    @DisplayName("no envía nada si el artesano está inactivo")
    void artesano_inactivo() {
        when(artesanos.buscarPorId(7L)).thenReturn(Optional.of(artesano(false, true)));

        notificador.avisarAprobada(inscripcion);

        verifyNoInteractions(correo);
    }

    @Test
    @DisplayName("no envía nada si el correo del artesano no está verificado")
    void correo_sin_verificar() {
        when(artesanos.buscarPorId(7L)).thenReturn(Optional.of(artesano(true, false)));

        notificador.avisarRechazada(inscripcion, false);

        verifyNoInteractions(correo);
    }

    @Test
    @DisplayName("no envía nada si el artesano o la edición no existen")
    void artesano_o_edicion_inexistentes() {
        when(artesanos.buscarPorId(7L)).thenReturn(Optional.empty());
        notificador.avisarAprobada(inscripcion);

        when(artesanos.buscarPorId(7L)).thenReturn(Optional.of(artesano(true, true)));
        when(ediciones.buscarPorId(4L)).thenReturn(Optional.empty());
        notificador.avisarAprobada(inscripcion);

        verifyNoInteractions(correo);
    }

    @Test
    @DisplayName("un fallo al enviar el correo no se propaga: la solicitud igual se procesa")
    void un_fallo_no_se_propaga() {
        org.mockito.Mockito.doThrow(new RuntimeException("SMTP caído"))
                .when(correo).enviar(anyString(), anyString(), any());

        assertDoesNotThrow(() -> notificador.avisarAprobada(inscripcion));
    }
}