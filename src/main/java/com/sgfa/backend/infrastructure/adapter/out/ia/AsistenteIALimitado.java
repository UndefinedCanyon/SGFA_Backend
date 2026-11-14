package com.sgfa.backend.infrastructure.adapter.out.ia;

import com.sgfa.backend.application.exception.LimiteUsoAsistenteException;
import com.sgfa.backend.application.port.out.AsistenteIAPort;
import com.sgfa.backend.domain.model.ContextoEdicion;
import com.sgfa.backend.domain.model.ResultadoAsistente;
import com.sgfa.backend.domain.model.ResumenSolicitud;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Decorador que limita cuántas llamadas por minuto se hacen al proveedor real, para no agotar su cuota
 * gratuita (que se cuenta por proyecto). Es un límite en memoria, suficiente para una sola instancia.
 */
public class AsistenteIALimitado implements AsistenteIAPort {

    private static final Duration VENTANA = Duration.ofMinutes(1);

    private final AsistenteIAPort delegado;
    private final int maximoPorMinuto;
    private final Clock reloj;
    private final Deque<Long> llamadas = new ArrayDeque<>();

    public AsistenteIALimitado(AsistenteIAPort delegado, int maximoPorMinuto, Clock reloj) {
        if (maximoPorMinuto < 1) {
            throw new IllegalArgumentException("El máximo de llamadas por minuto debe ser al menos 1.");
        }
        this.delegado = delegado;
        this.maximoPorMinuto = maximoPorMinuto;
        this.reloj = reloj;
    }

    @Override
    public String proveedor() {
        return delegado.proveedor();
    }

    @Override
    public ResultadoAsistente priorizarSolicitudes(ContextoEdicion contexto, List<ResumenSolicitud> solicitudes) {
        reservarLlamada();
        return delegado.priorizarSolicitudes(contexto, solicitudes);
    }

    private synchronized void reservarLlamada() {
        long ahora = reloj.millis();
        while (!llamadas.isEmpty() && ahora - llamadas.peekFirst() >= VENTANA.toMillis()) {
            llamadas.pollFirst();
        }
        if (llamadas.size() >= maximoPorMinuto) {
            long espera = Math.max(1, (VENTANA.toMillis() - (ahora - llamadas.peekFirst()) + 999) / 1000);
            throw new LimiteUsoAsistenteException(
                    "Pediste demasiados análisis seguidos. Espera " + espera + " segundos e intenta de nuevo.");
        }
        llamadas.addLast(ahora);
    }
}
