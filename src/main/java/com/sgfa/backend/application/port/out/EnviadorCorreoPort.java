package com.sgfa.backend.application.port.out;

public interface EnviadorCorreoPort {

    /** Envía el correo sin bloquear a quien llama. Un fallo de envío no debe romper la operación. */
    void enviar(String destinatario, String asunto, String cuerpoHtml);
}