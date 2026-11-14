package com.sgfa.backend.infrastructure.adapter.out.ia;

import com.sgfa.backend.application.exception.LimiteUsoAsistenteException;
import com.sgfa.backend.application.port.out.AsistenteIAPort;
import com.sgfa.backend.domain.model.ContextoEdicion;
import com.sgfa.backend.domain.model.ResultadoAsistente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsistenteIALimitadoTest {

    /** Reloj que solo avanza cuando la prueba lo pide. */
    private static final class RelojManual extends Clock {
        private long milisegundos = 1_000_000L;

        void avanzar(long ms) {
            milisegundos += ms;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(milisegundos);
        }
    }

    private final AsistenteIAPort delegado = mock(AsistenteIAPort.class);
    private final RelojManual reloj = new RelojManual();
    private static final ContextoEdicion CONTEXTO = null;

    private AsistenteIALimitado limitado(int maximo) {
        when(delegado.proveedor()).thenReturn("Real");
        when(delegado.priorizarSolicitudes(any(), any())).thenReturn(new ResultadoAsistente("ok", List.of()));
        return new AsistenteIALimitado(delegado, maximo, reloj);
    }

    @Test
    @DisplayName("el máximo por minuto debe ser al menos 1")
    void maximo_invalido() {
        assertThrows(IllegalArgumentException.class, () -> new AsistenteIALimitado(delegado, 0, reloj));
    }

    @Test
    @DisplayName("expone el proveedor del asistente que envuelve")
    void proveedor() {
        assertEquals("Real", limitado(2).proveedor());
    }

    @Test
    @DisplayName("deja pasar hasta el máximo y rechaza la siguiente llamada sin tocar al proveedor")
    void rechaza_al_superar_el_limite() {
        AsistenteIALimitado asistente = limitado(2);

        asistente.priorizarSolicitudes(CONTEXTO, List.of());
        asistente.priorizarSolicitudes(CONTEXTO, List.of());
        LimiteUsoAsistenteException ex = assertThrows(LimiteUsoAsistenteException.class,
                () -> asistente.priorizarSolicitudes(CONTEXTO, List.of()));

        assertTrue(ex.getMessage().contains("Espera 60 segundos"));
        verify(delegado, times(2)).priorizarSolicitudes(any(), any());
    }

    @Test
    @DisplayName("el mensaje indica cuánto falta para poder intentar de nuevo")
    void espera_restante() {
        AsistenteIALimitado asistente = limitado(1);
        asistente.priorizarSolicitudes(CONTEXTO, List.of());
        reloj.avanzar(45_000);

        LimiteUsoAsistenteException ex = assertThrows(LimiteUsoAsistenteException.class,
                () -> asistente.priorizarSolicitudes(CONTEXTO, List.of()));

        assertTrue(ex.getMessage().contains("Espera 15 segundos"));
    }

    @Test
    @DisplayName("pasado un minuto vuelve a permitir llamadas")
    void se_libera_con_el_tiempo() {
        AsistenteIALimitado asistente = limitado(1);
        asistente.priorizarSolicitudes(CONTEXTO, List.of());

        reloj.avanzar(60_000);
        asistente.priorizarSolicitudes(CONTEXTO, List.of());

        verify(delegado, times(2)).priorizarSolicitudes(any(), any());
    }
}
