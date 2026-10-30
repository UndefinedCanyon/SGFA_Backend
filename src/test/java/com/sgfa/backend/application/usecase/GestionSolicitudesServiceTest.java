package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.CupoAgotadoException;
import com.sgfa.backend.application.exception.InscripcionDuplicadaException;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Inscripcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionSolicitudesServiceTest {

    @Mock
    private InscripcionRepositoryPort inscripcionRepositoryPort;
    @Mock
    private EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;
    @Mock
    private NotificadorSolicitud notificadorSolicitud;

    private Inscripcion inscripcion(String estado) {
        return new Inscripcion(10L, 7L, 20L, LocalDate.of(2026, 10, 1), estado);
    }

    private EdicionFeria edicion(Integer cupoMaximo) {
        return new EdicionFeria(20L, 1L, 3L, LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 7), true, cupoMaximo);
    }

    @Nested
    @DisplayName("SolicitarParticipacionService")
    class Solicitar {

        @Test
        @DisplayName("Crea la solicitud pendiente, con la fecha de hoy")
        void crea_solicitud_pendiente() {
            SolicitarParticipacionService servicio = new SolicitarParticipacionService(inscripcionRepositoryPort);
            when(inscripcionRepositoryPort.existeInscripcion(7L, 20L)).thenReturn(false);
            when(inscripcionRepositoryPort.guardar(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

            servicio.solicitar(7L, 20L);

            ArgumentCaptor<Inscripcion> captor = ArgumentCaptor.forClass(Inscripcion.class);
            verify(inscripcionRepositoryPort).guardar(captor.capture());
            Inscripcion guardada = captor.getValue();

            assertNull(guardada.getId());
            assertEquals(7L, guardada.getIdArtesano());
            assertEquals(20L, guardada.getIdEdicionFeria());
            assertEquals("PENDIENTE", guardada.getEstado());
            assertEquals(LocalDate.now(), guardada.getFechaInscripcion());
        }

        @Test
        @DisplayName("No permite solicitar dos veces la misma edición")
        void solicitud_duplicada() {
            SolicitarParticipacionService servicio = new SolicitarParticipacionService(inscripcionRepositoryPort);
            when(inscripcionRepositoryPort.existeInscripcion(7L, 20L)).thenReturn(true);

            InscripcionDuplicadaException error = assertThrows(InscripcionDuplicadaException.class,
                    () -> servicio.solicitar(7L, 20L));

            assertEquals("Ya has solicitado participar en esta edición.", error.getMessage());
            verify(inscripcionRepositoryPort, never()).guardar(any());
        }
    }

    @Nested
    @DisplayName("AprobarSolicitudService")
    class Aprobar {

        private AprobarSolicitudService servicio() {
            return new AprobarSolicitudService(inscripcionRepositoryPort, edicionFeriaRepositoryPort,
                    notificadorSolicitud);
        }

        @Test
        @DisplayName("Aprueba una solicitud pendiente cuando la edición no tiene cupo definido")
        void aprueba_sin_cupo() {
            when(inscripcionRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(inscripcion("PENDIENTE")));
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(null)));
            when(inscripcionRepositoryPort.guardar(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

            Inscripcion resultado = servicio().aprobar(10L);

            assertEquals("APROBADA", resultado.getEstado());
            verify(inscripcionRepositoryPort, never()).contarAprobadasPorEdicion(anyLong());
            verify(notificadorSolicitud).avisarAprobada(resultado);
        }

        @Test
        @DisplayName("Aprueba si todavía queda cupo")
        void aprueba_con_cupo_disponible() {
            when(inscripcionRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(inscripcion("PENDIENTE")));
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(3)));
            when(inscripcionRepositoryPort.contarAprobadasPorEdicion(20L)).thenReturn(2L);
            when(inscripcionRepositoryPort.guardar(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

            Inscripcion resultado = servicio().aprobar(10L);

            assertEquals("APROBADA", resultado.getEstado());
            verify(notificadorSolicitud).avisarAprobada(resultado);
        }

        @Test
        @DisplayName("Rechaza la aprobación si el cupo ya está lleno, sin guardar ni avisar")
        void cupo_agotado() {
            when(inscripcionRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(inscripcion("PENDIENTE")));
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(2)));
            when(inscripcionRepositoryPort.contarAprobadasPorEdicion(20L)).thenReturn(2L);

            CupoAgotadoException error = assertThrows(CupoAgotadoException.class, () -> servicio().aprobar(10L));

            assertTrue(error.getMessage().contains("2"));
            verify(inscripcionRepositoryPort, never()).guardar(any());
            verifyNoInteractions(notificadorSolicitud);
        }

        @Test
        @DisplayName("Una solicitud rechazada se puede aprobar después, si hay cupo")
        void aprueba_una_rechazada() {
            when(inscripcionRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(inscripcion("RECHAZADA")));
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(null)));
            when(inscripcionRepositoryPort.guardar(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

            assertEquals("APROBADA", servicio().aprobar(10L).getEstado());
        }

        @Test
        @DisplayName("Si ya estaba aprobada no se cuenta de nuevo ni se vuelve a avisar")
        void ya_aprobada() {
            Inscripcion aprobada = inscripcion("APROBADA");
            when(inscripcionRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(aprobada));

            assertSame(aprobada, servicio().aprobar(10L));

            verify(inscripcionRepositoryPort, never()).guardar(any());
            verifyNoInteractions(edicionFeriaRepositoryPort, notificadorSolicitud);
        }

        @Test
        @DisplayName("Una solicitud que no existe se rechaza")
        void solicitud_inexistente() {
            when(inscripcionRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio().aprobar(99L));

            assertEquals("La solicitud no existe.", error.getMessage());
        }

        @Test
        @DisplayName("Una edición que no existe se rechaza sin guardar")
        void edicion_inexistente() {
            when(inscripcionRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(inscripcion("PENDIENTE")));
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio().aprobar(10L));

            assertEquals("La edición no existe.", error.getMessage());
            verify(inscripcionRepositoryPort, never()).guardar(any());
        }
    }

    @Nested
    @DisplayName("RechazarSolicitudService")
    class Rechazar {

        private RechazarSolicitudService servicio() {
            return new RechazarSolicitudService(inscripcionRepositoryPort, notificadorSolicitud);
        }

        @Test
        @DisplayName("Rechaza una solicitud pendiente y avisa que no era una participación aprobada")
        void rechaza_pendiente() {
            when(inscripcionRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(inscripcion("PENDIENTE")));
            when(inscripcionRepositoryPort.guardar(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

            Inscripcion resultado = servicio().rechazar(10L);

            assertEquals("RECHAZADA", resultado.getEstado());
            verify(notificadorSolicitud).avisarRechazada(resultado, false);
        }

        @Test
        @DisplayName("Rechazar una aprobada cancela la participación y lo indica en el aviso")
        void rechaza_aprobada() {
            when(inscripcionRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(inscripcion("APROBADA")));
            when(inscripcionRepositoryPort.guardar(any(Inscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

            Inscripcion resultado = servicio().rechazar(10L);

            assertEquals("RECHAZADA", resultado.getEstado());
            verify(notificadorSolicitud).avisarRechazada(resultado, true);
        }

        @Test
        @DisplayName("Si ya estaba rechazada no se cambia nada ni se vuelve a avisar")
        void ya_rechazada() {
            Inscripcion rechazada = inscripcion("RECHAZADA");
            when(inscripcionRepositoryPort.buscarPorId(10L)).thenReturn(Optional.of(rechazada));

            assertSame(rechazada, servicio().rechazar(10L));

            verify(inscripcionRepositoryPort, never()).guardar(any());
            verifyNoInteractions(notificadorSolicitud);
        }

        @Test
        @DisplayName("Una solicitud que no existe se rechaza")
        void solicitud_inexistente() {
            when(inscripcionRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio().rechazar(99L));

            assertEquals("La solicitud no existe.", error.getMessage());
            verifyNoInteractions(notificadorSolicitud);
        }
    }
}