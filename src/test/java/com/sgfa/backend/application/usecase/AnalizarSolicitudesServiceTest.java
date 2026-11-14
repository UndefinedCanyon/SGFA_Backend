package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.AsistenteNoDisponibleException;
import com.sgfa.backend.application.exception.DatoInvalidoException;
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
import com.sgfa.backend.domain.model.PrioridadSolicitud;
import com.sgfa.backend.domain.model.Producto;
import com.sgfa.backend.domain.model.ResultadoAsistente;
import com.sgfa.backend.domain.model.ResumenSolicitud;
import com.sgfa.backend.domain.model.SugerenciaSolicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalizarSolicitudesServiceTest {

    private static final Long EDICION = 5L;

    private InscripcionRepositoryPort inscripciones;
    private EdicionFeriaRepositoryPort ediciones;
    private FeriaRepositoryPort ferias;
    private LugarRepositoryPort lugares;
    private ArtesanoRepositoryPort artesanos;
    private ProductoRepositoryPort productos;
    private AsistenteIAPort asistente;
    private AnalizarSolicitudesService servicio;

    @BeforeEach
    void preparar() {
        inscripciones = mock(InscripcionRepositoryPort.class);
        ediciones = mock(EdicionFeriaRepositoryPort.class);
        ferias = mock(FeriaRepositoryPort.class);
        lugares = mock(LugarRepositoryPort.class);
        artesanos = mock(ArtesanoRepositoryPort.class);
        productos = mock(ProductoRepositoryPort.class);
        asistente = mock(AsistenteIAPort.class);
        servicio = new AnalizarSolicitudesService(inscripciones, ediciones, ferias, lugares, artesanos, productos, asistente);

        when(asistente.proveedor()).thenReturn("Prueba");
        when(ediciones.buscarPorId(EDICION)).thenReturn(Optional.of(
                new EdicionFeria(EDICION, 1L, 2L, LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 24), true, 4)));
        when(ferias.buscarPorId(1L)).thenReturn(Optional.of(new Feria(1L, "Feria Navideña", 1L, true)));
        when(lugares.buscarPorId(2L)).thenReturn(Optional.of(new Lugar(2L, "Parque Principal", "Cra 6", 4.3, -74.3)));
        when(inscripciones.contarAprobadasPorEdicion(EDICION)).thenReturn(1L);
    }

    private static Inscripcion inscripcion(long id, long idArtesano, long idEdicion, int dia, String estado) {
        return new Inscripcion(id, idArtesano, idEdicion, LocalDate.of(2026, 10, dia), estado);
    }

    private void artesano(long id, String emprendimiento, String descripcion) {
        when(artesanos.buscarPorId(id)).thenReturn(Optional.of(new Artesano(id, "Nombre Privado " + id,
                "privado" + id + "@correo.com", "hash", "1000" + id, "300" + id, emprendimiento, descripcion, true)));
    }

    private void productos(long idArtesano, String... nombres) {
        List<Producto> lista = new ArrayList<>();
        for (String nombre : nombres) {
            lista.add(new Producto(null, nombre, BigDecimal.TEN, 3, idArtesano));
        }
        when(productos.listarPorArtesano(idArtesano)).thenReturn(lista);
    }

    private List<ResumenSolicitud> resumenesEnviados() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ResumenSolicitud>> captor = ArgumentCaptor.forClass(List.class);
        verify(asistente).priorizarSolicitudes(any(), captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("sin id de edición pide indicarlo")
    void sin_id() {
        assertThrows(DatoInvalidoException.class, () -> servicio.analizar(null));
    }

    @Test
    @DisplayName("una edición inexistente lanza IllegalArgumentException")
    void edicion_inexistente() {
        when(ediciones.buscarPorId(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> servicio.analizar(99L));

        assertEquals("La edición no existe.", ex.getMessage());
    }

    @Test
    @DisplayName("sin solicitudes pendientes no llama a la IA")
    void sin_pendientes() {
        when(inscripciones.listarTodas()).thenReturn(List.of(
                inscripcion(1, 10, EDICION, 1, "APROBADA"),
                inscripcion(2, 11, 6L, 1, "PENDIENTE")));

        AnalisisSolicitudes analisis = servicio.analizar(EDICION);

        assertEquals(0, analisis.totalPendientes());
        assertEquals(0, analisis.analizadas());
        assertTrue(analisis.sugerencias().isEmpty());
        assertEquals("No hay solicitudes pendientes en esta edición.", analisis.resumen());
        assertEquals(3, analisis.cuposDisponibles());
        assertEquals("Prueba", analisis.proveedor());
        verify(asistente, never()).priorizarSolicitudes(any(), any());
    }

    @Test
    @DisplayName("analiza solo las pendientes de la edición, de la más antigua a la más reciente")
    void solo_pendientes_de_la_edicion_ordenadas() {
        when(inscripciones.listarTodas()).thenReturn(List.of(
                inscripcion(1, 10, EDICION, 3, "PENDIENTE"),
                inscripcion(2, 11, EDICION, 1, "PENDIENTE"),
                inscripcion(3, 12, EDICION, 2, "APROBADA"),
                inscripcion(4, 13, 6L, 2, "PENDIENTE")));
        artesano(10, "Taller Diez", "Ruanas de lana");
        artesano(11, "Taller Once", "Cerámica");
        productos(10, "Ruana", "Bufanda");
        productos(11);
        when(asistente.priorizarSolicitudes(any(), any())).thenReturn(new ResultadoAsistente("ok", List.of()));

        AnalisisSolicitudes analisis = servicio.analizar(EDICION);

        List<ResumenSolicitud> enviados = resumenesEnviados();
        assertEquals(List.of(2L, 1L), enviados.stream().map(ResumenSolicitud::idInscripcion).toList());
        assertEquals(List.of("Ruana", "Bufanda"), enviados.get(1).productos());
        assertTrue(enviados.get(0).productos().isEmpty());
        assertEquals(2, analisis.totalPendientes());
        assertEquals(2, analisis.analizadas());
    }

    @Test
    @DisplayName("no envía datos personales a la IA")
    void sin_datos_personales() {
        when(inscripciones.listarTodas()).thenReturn(List.of(inscripcion(1, 10, EDICION, 1, "PENDIENTE")));
        artesano(10, "Taller Diez", "Ruanas de lana");
        productos(10, "Ruana");
        when(asistente.priorizarSolicitudes(any(), any())).thenReturn(new ResultadoAsistente("ok", List.of()));

        servicio.analizar(EDICION);

        String enviado = resumenesEnviados().toString();
        assertFalse(enviado.contains("Nombre Privado"));
        assertFalse(enviado.contains("privado10@correo.com"));
        assertFalse(enviado.contains("100010"));
        assertTrue(enviado.contains("Taller Diez"));
    }

    @Test
    @DisplayName("el contexto lleva el nombre de la feria, el lugar y el cupo")
    void contexto_de_la_edicion() {
        when(inscripciones.listarTodas()).thenReturn(List.of(inscripcion(1, 10, EDICION, 1, "PENDIENTE")));
        artesano(10, "Taller Diez", "Ruanas");
        productos(10);
        when(asistente.priorizarSolicitudes(any(), any())).thenReturn(new ResultadoAsistente("ok", List.of()));

        servicio.analizar(EDICION);

        ArgumentCaptor<ContextoEdicion> captor = ArgumentCaptor.forClass(ContextoEdicion.class);
        verify(asistente).priorizarSolicitudes(captor.capture(), any());
        ContextoEdicion contexto = captor.getValue();
        assertEquals("Feria Navideña", contexto.nombreFeria());
        assertEquals("Parque Principal", contexto.lugar());
        assertEquals(4, contexto.cupoMaximo());
        assertEquals(1, contexto.aprobados());
        assertEquals(3, contexto.cuposDisponibles());
        assertEquals(1, contexto.totalPendientes());
    }

    @Test
    @DisplayName("si faltan la feria o el lugar usa nombres de respaldo")
    void nombres_de_respaldo() {
        when(ferias.buscarPorId(1L)).thenReturn(Optional.empty());
        when(lugares.buscarPorId(2L)).thenReturn(Optional.empty());
        when(inscripciones.listarTodas()).thenReturn(List.of(inscripcion(1, 10, EDICION, 1, "PENDIENTE")));
        artesano(10, "Taller Diez", "Ruanas");
        productos(10);
        when(asistente.priorizarSolicitudes(any(), any())).thenReturn(new ResultadoAsistente("ok", List.of()));

        servicio.analizar(EDICION);

        ArgumentCaptor<ContextoEdicion> captor = ArgumentCaptor.forClass(ContextoEdicion.class);
        verify(asistente).priorizarSolicitudes(captor.capture(), any());
        assertEquals("Feria #1", captor.getValue().nombreFeria());
        assertEquals("Lugar #2", captor.getValue().lugar());
    }

    @Test
    @DisplayName("una edición sin límite de cupo no tiene cupos disponibles que calcular")
    void sin_limite_de_cupo() {
        when(ediciones.buscarPorId(EDICION)).thenReturn(Optional.of(
                new EdicionFeria(EDICION, 1L, 2L, LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 24), true, null)));
        when(inscripciones.listarTodas()).thenReturn(List.of());

        AnalisisSolicitudes analisis = servicio.analizar(EDICION);

        assertNull(analisis.cuposDisponibles());
    }

    @Test
    @DisplayName("omite a los artesanos que ya no existen pero cuenta sus solicitudes como pendientes")
    void artesano_inexistente() {
        when(inscripciones.listarTodas()).thenReturn(List.of(
                inscripcion(1, 10, EDICION, 1, "PENDIENTE"),
                inscripcion(2, 99, EDICION, 2, "PENDIENTE")));
        artesano(10, "Taller Diez", "Ruanas");
        productos(10);
        when(artesanos.buscarPorId(99L)).thenReturn(Optional.empty());
        when(asistente.priorizarSolicitudes(any(), any())).thenReturn(new ResultadoAsistente("ok", List.of()));

        AnalisisSolicitudes analisis = servicio.analizar(EDICION);

        assertEquals(2, analisis.totalPendientes());
        assertEquals(1, analisis.analizadas());
    }

    @Test
    @DisplayName("si ningún artesano existe tampoco llama a la IA")
    void ningun_artesano_existe() {
        when(inscripciones.listarTodas()).thenReturn(List.of(inscripcion(1, 99, EDICION, 1, "PENDIENTE")));
        when(artesanos.buscarPorId(99L)).thenReturn(Optional.empty());

        AnalisisSolicitudes analisis = servicio.analizar(EDICION);

        assertEquals(0, analisis.analizadas());
        verify(asistente, never()).priorizarSolicitudes(any(), any());
    }

    @Test
    @DisplayName("limita a 15 solicitudes y recorta productos y textos largos")
    void limites() {
        List<Inscripcion> muchas = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            muchas.add(inscripcion(i, 100 + i, EDICION, Math.min(i, 28), "PENDIENTE"));
            artesano(100 + i, "Taller " + i, "x".repeat(500));
            productos(100 + i, "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10");
        }
        when(inscripciones.listarTodas()).thenReturn(muchas);
        when(asistente.priorizarSolicitudes(any(), any())).thenReturn(new ResultadoAsistente("ok", List.of()));

        AnalisisSolicitudes analisis = servicio.analizar(EDICION);

        List<ResumenSolicitud> enviados = resumenesEnviados();
        assertEquals(20, analisis.totalPendientes());
        assertEquals(15, analisis.analizadas());
        assertEquals(15, enviados.size());
        assertEquals(8, enviados.get(0).productos().size());
        assertEquals(AnalizarSolicitudesService.MAX_TEXTO_DESCRIPCION + 1, enviados.get(0).descripcion().length());
        assertTrue(enviados.get(0).descripcion().endsWith("…"));
    }

    @Test
    @DisplayName("ordena las sugerencias con la prioridad alta primero, descarta ids inventados y repetidos y completa las que faltan")
    void sugerencias_depuradas() {
        when(inscripciones.listarTodas()).thenReturn(List.of(
                inscripcion(1, 10, EDICION, 1, "PENDIENTE"),
                inscripcion(2, 11, EDICION, 2, "PENDIENTE"),
                inscripcion(3, 12, EDICION, 3, "PENDIENTE")));
        for (long id : new long[]{10, 11, 12}) {
            artesano(id, "Taller " + id, "Descripción");
            productos(id);
        }
        when(asistente.priorizarSolicitudes(any(), any())).thenReturn(new ResultadoAsistente("resumen general", List.of(
                new SugerenciaSolicitud(1L, PrioridadSolicitud.BAJA, "poco para ofrecer"),
                new SugerenciaSolicitud(3L, PrioridadSolicitud.ALTA, "buen catálogo"),
                new SugerenciaSolicitud(777L, PrioridadSolicitud.ALTA, "inventada"),
                new SugerenciaSolicitud(3L, PrioridadSolicitud.BAJA, "repetida"))));

        AnalisisSolicitudes analisis = servicio.analizar(EDICION);

        assertEquals("resumen general", analisis.resumen());
        List<SugerenciaSolicitud> sugerencias = analisis.sugerencias();
        assertEquals(3, sugerencias.size());
        assertEquals(3L, sugerencias.get(0).idInscripcion());
        assertEquals(PrioridadSolicitud.ALTA, sugerencias.get(0).prioridad());
        assertEquals(2L, sugerencias.get(1).idInscripcion());
        assertEquals(PrioridadSolicitud.MEDIA, sugerencias.get(1).prioridad());
        assertEquals(AnalizarSolicitudesService.SIN_SUGERENCIA, sugerencias.get(1).motivo());
        assertEquals(1L, sugerencias.get(2).idInscripcion());
        assertEquals("poco para ofrecer", sugerencias.get(2).motivo());
    }

    @Test
    @DisplayName("tolera una lista de sugerencias nula o con elementos nulos")
    void sugerencias_nulas() {
        when(inscripciones.listarTodas()).thenReturn(List.of(inscripcion(1, 10, EDICION, 1, "PENDIENTE")));
        artesano(10, "Taller Diez", "Ruanas");
        productos(10);
        List<SugerenciaSolicitud> conNulo = new ArrayList<>();
        conNulo.add(null);
        when(asistente.priorizarSolicitudes(any(), any())).thenReturn(new ResultadoAsistente("ok", conNulo));

        AnalisisSolicitudes analisis = servicio.analizar(EDICION);

        assertEquals(1, analisis.sugerencias().size());
        assertEquals(AnalizarSolicitudesService.SIN_SUGERENCIA, analisis.sugerencias().get(0).motivo());
    }

    @Test
    @DisplayName("si la IA falla, el error llega tal cual para que el controlador lo explique")
    void falla_la_ia() {
        when(inscripciones.listarTodas()).thenReturn(List.of(inscripcion(1, 10, EDICION, 1, "PENDIENTE")));
        artesano(10, "Taller Diez", "Ruanas");
        productos(10);
        when(asistente.priorizarSolicitudes(any(), any())).thenThrow(new AsistenteNoDisponibleException("sin servicio"));

        assertThrows(AsistenteNoDisponibleException.class, () -> servicio.analizar(EDICION));
    }

    @Test
    @DisplayName("limpiar convierte null en texto vacío y colapsa espacios y saltos de línea")
    void limpiar_texto() {
        assertEquals("", AnalizarSolicitudesService.limpiar(null, 10));
        assertEquals("a b c", AnalizarSolicitudesService.limpiar("  a \n\n b\t c  ", 10));
        assertEquals("abc…", AnalizarSolicitudesService.limpiar("abcdef", 3));
    }
}
