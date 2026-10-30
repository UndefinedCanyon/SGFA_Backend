package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.InscripcionRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.OcupacionEdicion;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuposEdicionServiceTest {

    @Mock
    private EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;
    @Mock
    private InscripcionRepositoryPort inscripcionRepositoryPort;

    private EdicionFeria edicion(Integer cupoMaximo) {
        return new EdicionFeria(20L, 1L, 3L, LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 7), true, cupoMaximo);
    }

    @Nested
    @DisplayName("ConfigurarCupoEdicionService")
    class Configurar {

        private ConfigurarCupoEdicionService servicio() {
            return new ConfigurarCupoEdicionService(edicionFeriaRepositoryPort, inscripcionRepositoryPort);
        }

        @Test
        @DisplayName("Define un cupo mayor a los artesanos ya aprobados")
        void define_cupo() {
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(null)));
            when(inscripcionRepositoryPort.contarAprobadasPorEdicion(20L)).thenReturn(2L);
            when(edicionFeriaRepositoryPort.guardar(any(EdicionFeria.class))).thenAnswer(inv -> inv.getArgument(0));

            EdicionFeria resultado = servicio().configurar(20L, 5);

            assertEquals(5, resultado.getCupoMaximo());
        }

        @Test
        @DisplayName("El cupo puede ser igual al número de aprobados")
        void cupo_igual_a_aprobados() {
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(null)));
            when(inscripcionRepositoryPort.contarAprobadasPorEdicion(20L)).thenReturn(2L);
            when(edicionFeriaRepositoryPort.guardar(any(EdicionFeria.class))).thenAnswer(inv -> inv.getArgument(0));

            assertEquals(2, servicio().configurar(20L, 2).getCupoMaximo());
        }

        @Test
        @DisplayName("Un cupo nulo quita el límite, sin consultar los aprobados")
        void quita_el_limite() {
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(10)));
            when(edicionFeriaRepositoryPort.guardar(any(EdicionFeria.class))).thenAnswer(inv -> inv.getArgument(0));

            EdicionFeria resultado = servicio().configurar(20L, null);

            assertNull(resultado.getCupoMaximo());
            verify(inscripcionRepositoryPort, never()).contarAprobadasPorEdicion(anyLong());
        }

        @Test
        @DisplayName("Un cupo menor a 1 se rechaza")
        void cupo_menor_a_uno() {
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(null)));

            DatoInvalidoException error = assertThrows(DatoInvalidoException.class,
                    () -> servicio().configurar(20L, 0));

            assertEquals("El cupo debe ser de al menos 1 artesano.", error.getMessage());
            verify(edicionFeriaRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Un cupo menor a los ya aprobados se rechaza y no se guarda")
        void cupo_menor_a_aprobados() {
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(null)));
            when(inscripcionRepositoryPort.contarAprobadasPorEdicion(20L)).thenReturn(3L);

            DatoInvalidoException error = assertThrows(DatoInvalidoException.class,
                    () -> servicio().configurar(20L, 2));

            assertTrue(error.getMessage().contains("3"));
            verify(edicionFeriaRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Una edición que no existe se rechaza")
        void edicion_inexistente() {
            when(edicionFeriaRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio().configurar(99L, 5));

            assertEquals("La edición no existe.", error.getMessage());
        }
    }

    @Nested
    @DisplayName("ConsultarOcupacionEdicionService")
    class Ocupacion {

        private ConsultarOcupacionEdicionService servicio() {
            return new ConsultarOcupacionEdicionService(edicionFeriaRepositoryPort, inscripcionRepositoryPort);
        }

        @Test
        @DisplayName("Calcula los cupos disponibles")
        void cupos_disponibles() {
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(10)));
            when(inscripcionRepositoryPort.contarAprobadasPorEdicion(20L)).thenReturn(4L);

            OcupacionEdicion ocupacion = servicio().consultar(20L);

            assertEquals(10, ocupacion.getCupoMaximo());
            assertEquals(4L, ocupacion.getAprobados());
            assertEquals(6, ocupacion.getDisponibles());
        }

        @Test
        @DisplayName("Sin cupo definido no hay cupos disponibles que calcular")
        void sin_cupo() {
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(null)));
            when(inscripcionRepositoryPort.contarAprobadasPorEdicion(20L)).thenReturn(4L);

            OcupacionEdicion ocupacion = servicio().consultar(20L);

            assertNull(ocupacion.getCupoMaximo());
            assertNull(ocupacion.getDisponibles());
            assertEquals(4L, ocupacion.getAprobados());
        }

        @Test
        @DisplayName("Los disponibles nunca son negativos, aunque haya más aprobados que cupo")
        void disponibles_nunca_negativos() {
            when(edicionFeriaRepositoryPort.buscarPorId(20L)).thenReturn(Optional.of(edicion(2)));
            when(inscripcionRepositoryPort.contarAprobadasPorEdicion(20L)).thenReturn(5L);

            assertEquals(0, servicio().consultar(20L).getDisponibles());
        }

        @Test
        @DisplayName("Una edición que no existe se rechaza")
        void edicion_inexistente() {
            when(edicionFeriaRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio().consultar(99L));

            assertEquals("La edición no existe.", error.getMessage());
        }
    }
}