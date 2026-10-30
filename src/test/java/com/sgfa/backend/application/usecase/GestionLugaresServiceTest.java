package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.domain.model.Lugar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionLugaresServiceTest {

    @Mock
    private LugarRepositoryPort lugarRepositoryPort;

    private Lugar lugarExistente() {
        return new Lugar(5L, "Parque Viejo", "Calle 1 # 2-3", 4.0, -74.0, true);
    }

    @Nested
    @DisplayName("CrearLugarService")
    class Crear {

        @Test
        @DisplayName("Crea el lugar activo, con los textos recortados")
        void crea_lugar() {
            CrearLugarService servicio = new CrearLugarService(lugarRepositoryPort);
            when(lugarRepositoryPort.guardar(any(Lugar.class))).thenAnswer(inv -> inv.getArgument(0));

            servicio.crear("  Parque Principal ", " Calle 6 # 7-10  ", 4.3369, -74.3636);

            ArgumentCaptor<Lugar> captor = ArgumentCaptor.forClass(Lugar.class);
            verify(lugarRepositoryPort).guardar(captor.capture());
            Lugar guardado = captor.getValue();

            assertNull(guardado.getId());
            assertEquals("Parque Principal", guardado.getNombre());
            assertEquals("Calle 6 # 7-10", guardado.getDireccion());
            assertEquals(4.3369, guardado.getLatitud());
            assertEquals(-74.3636, guardado.getLongitud());
            assertTrue(guardado.isActivo());
        }

        @Test
        @DisplayName("Devuelve lo que entrega el repositorio")
        void devuelve_el_guardado() {
            CrearLugarService servicio = new CrearLugarService(lugarRepositoryPort);
            Lugar conId = new Lugar(9L, "Parque", "Calle 1", null, null);
            when(lugarRepositoryPort.guardar(any(Lugar.class))).thenReturn(conId);

            assertSame(conId, servicio.crear("Parque", "Calle 1", null, null));
        }

        @Test
        @DisplayName("Un lugar inválido no se guarda")
        void lugar_invalido() {
            CrearLugarService servicio = new CrearLugarService(lugarRepositoryPort);

            assertThrows(DatoInvalidoException.class, () -> servicio.crear(" ", "Calle 1", null, null));
            assertThrows(DatoInvalidoException.class, () -> servicio.crear("Parque", "Calle 1", 4.0, null));

            verifyNoInteractions(lugarRepositoryPort);
        }
    }

    @Nested
    @DisplayName("ActualizarLugarService")
    class Actualizar {

        @Test
        @DisplayName("Actualiza nombre, dirección y coordenadas del lugar")
        void actualiza_lugar() {
            ActualizarLugarService servicio = new ActualizarLugarService(lugarRepositoryPort);
            when(lugarRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(lugarExistente()));
            when(lugarRepositoryPort.guardar(any(Lugar.class))).thenAnswer(inv -> inv.getArgument(0));

            Lugar resultado = servicio.actualizar(5L, "  Parque Nuevo ", " Carrera 9 # 8-7 ", 4.5, -74.5);

            assertEquals(5L, resultado.getId());
            assertEquals("Parque Nuevo", resultado.getNombre());
            assertEquals("Carrera 9 # 8-7", resultado.getDireccion());
            assertEquals(4.5, resultado.getLatitud());
            assertEquals(-74.5, resultado.getLongitud());
            assertTrue(resultado.isActivo());
        }

        @Test
        @DisplayName("Se pueden quitar las coordenadas de un lugar")
        void quita_coordenadas() {
            ActualizarLugarService servicio = new ActualizarLugarService(lugarRepositoryPort);
            when(lugarRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(lugarExistente()));
            when(lugarRepositoryPort.guardar(any(Lugar.class))).thenAnswer(inv -> inv.getArgument(0));

            Lugar resultado = servicio.actualizar(5L, "Parque Viejo", "Calle 1 # 2-3", null, null);

            assertNull(resultado.getLatitud());
            assertNull(resultado.getLongitud());
        }

        @Test
        @DisplayName("Un lugar que no existe se rechaza sin guardar")
        void lugar_inexistente() {
            ActualizarLugarService servicio = new ActualizarLugarService(lugarRepositoryPort);
            when(lugarRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio.actualizar(99L, "Parque", "Calle 1", null, null));

            assertEquals("El lugar no existe.", error.getMessage());
            verify(lugarRepositoryPort, never()).guardar(any());
        }

        @Test
        @DisplayName("Datos inválidos se rechazan sin guardar")
        void datos_invalidos() {
            ActualizarLugarService servicio = new ActualizarLugarService(lugarRepositoryPort);
            when(lugarRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(lugarExistente()));

            assertThrows(DatoInvalidoException.class, () -> servicio.actualizar(5L, "", "Calle 1", null, null));

            verify(lugarRepositoryPort, never()).guardar(any());
        }
    }

    @Nested
    @DisplayName("CambiarEstadoLugarService")
    class CambiarEstado {

        @Test
        @DisplayName("Desactiva un lugar activo")
        void desactiva() {
            CambiarEstadoLugarService servicio = new CambiarEstadoLugarService(lugarRepositoryPort);
            when(lugarRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(lugarExistente()));

            servicio.cambiarEstado(5L, false);

            ArgumentCaptor<Lugar> captor = ArgumentCaptor.forClass(Lugar.class);
            verify(lugarRepositoryPort).guardar(captor.capture());
            assertFalse(captor.getValue().isActivo());
        }

        @Test
        @DisplayName("Reactiva un lugar desactivado")
        void reactiva() {
            CambiarEstadoLugarService servicio = new CambiarEstadoLugarService(lugarRepositoryPort);
            Lugar desactivado = new Lugar(5L, "Parque", "Calle 1", null, null, false);
            when(lugarRepositoryPort.buscarPorId(5L)).thenReturn(Optional.of(desactivado));

            servicio.cambiarEstado(5L, true);

            ArgumentCaptor<Lugar> captor = ArgumentCaptor.forClass(Lugar.class);
            verify(lugarRepositoryPort).guardar(captor.capture());
            assertTrue(captor.getValue().isActivo());
        }

        @Test
        @DisplayName("Un lugar que no existe se rechaza sin guardar")
        void lugar_inexistente() {
            CambiarEstadoLugarService servicio = new CambiarEstadoLugarService(lugarRepositoryPort);
            when(lugarRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> servicio.cambiarEstado(99L, false));

            assertEquals("El lugar no existe.", error.getMessage());
            verify(lugarRepositoryPort, never()).guardar(any());
        }
    }
}