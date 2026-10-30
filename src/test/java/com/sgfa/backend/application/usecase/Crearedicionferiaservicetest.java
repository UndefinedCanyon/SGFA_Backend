package com.sgfa.backend.application.usecase;

import com.sgfa.backend.application.exception.DatoInvalidoException;
import com.sgfa.backend.application.port.out.EdicionFeriaRepositoryPort;
import com.sgfa.backend.application.port.out.LugarRepositoryPort;
import com.sgfa.backend.domain.model.EdicionFeria;
import com.sgfa.backend.domain.model.Lugar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearEdicionFeriaServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 12, 5);
    private static final LocalDate FIN = LocalDate.of(2026, 12, 7);

    @Mock
    private EdicionFeriaRepositoryPort edicionFeriaRepositoryPort;
    @Mock
    private LugarRepositoryPort lugarRepositoryPort;

    private CrearEdicionFeriaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new CrearEdicionFeriaService(edicionFeriaRepositoryPort, lugarRepositoryPort);
    }

    private Lugar lugar(boolean activo) {
        return new Lugar(3L, "Parque Principal", "Calle 6 # 7-10", null, null, activo);
    }

    @Test
    @DisplayName("Crea la edición activa y sin cupo, con los datos indicados")
    void crea_edicion() {
        when(lugarRepositoryPort.buscarPorId(3L)).thenReturn(Optional.of(lugar(true)));
        when(edicionFeriaRepositoryPort.guardar(any(EdicionFeria.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.crear(1L, 3L, INICIO, FIN);

        ArgumentCaptor<EdicionFeria> captor = ArgumentCaptor.forClass(EdicionFeria.class);
        verify(edicionFeriaRepositoryPort).guardar(captor.capture());
        EdicionFeria guardada = captor.getValue();

        assertNull(guardada.getId());
        assertEquals(1L, guardada.getIdFeria());
        assertEquals(3L, guardada.getIdLugar());
        assertEquals(INICIO, guardada.getFechaInicio());
        assertEquals(FIN, guardada.getFechaFin());
        assertTrue(guardada.isActivo());
        assertNull(guardada.getCupoMaximo());
    }

    @Test
    @DisplayName("Una edición de un solo día (inicio igual a fin) es válida")
    void edicion_de_un_dia() {
        when(lugarRepositoryPort.buscarPorId(3L)).thenReturn(Optional.of(lugar(true)));
        when(edicionFeriaRepositoryPort.guardar(any(EdicionFeria.class))).thenAnswer(inv -> inv.getArgument(0));

        EdicionFeria resultado = servicio.crear(1L, 3L, INICIO, INICIO);

        assertEquals(INICIO, resultado.getFechaInicio());
        assertEquals(INICIO, resultado.getFechaFin());
    }

    @Test
    @DisplayName("Sin fecha de inicio o de fin se rechaza antes de consultar nada")
    void fechas_nulas() {
        assertThrows(DatoInvalidoException.class, () -> servicio.crear(1L, 3L, null, FIN));
        assertThrows(DatoInvalidoException.class, () -> servicio.crear(1L, 3L, INICIO, null));

        verifyNoInteractions(lugarRepositoryPort, edicionFeriaRepositoryPort);
    }

    @Test
    @DisplayName("Una fecha de fin anterior a la de inicio se rechaza")
    void fin_antes_de_inicio() {
        DatoInvalidoException error = assertThrows(DatoInvalidoException.class,
                () -> servicio.crear(1L, 3L, FIN, INICIO));

        assertEquals("La fecha de fin no puede ser anterior a la fecha de inicio.", error.getMessage());
        verifyNoInteractions(lugarRepositoryPort, edicionFeriaRepositoryPort);
    }

    @Test
    @DisplayName("Un lugar que no existe se rechaza sin guardar")
    void lugar_inexistente() {
        when(lugarRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.crear(1L, 99L, INICIO, FIN));

        assertEquals("El lugar indicado no existe.", error.getMessage());
        verify(edicionFeriaRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Un lugar desactivado se rechaza y el mensaje incluye su nombre")
    void lugar_desactivado() {
        when(lugarRepositoryPort.buscarPorId(3L)).thenReturn(Optional.of(lugar(false)));

        DatoInvalidoException error = assertThrows(DatoInvalidoException.class,
                () -> servicio.crear(1L, 3L, INICIO, FIN));

        assertTrue(error.getMessage().contains("Parque Principal"));
        verify(edicionFeriaRepositoryPort, never()).guardar(any());
    }
}