package com.sgfa.backend.infrastructure.adapter.out.mail;

import com.sgfa.backend.application.port.out.EnviadorCorreoPort;
import jakarta.annotation.PreDestroy;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CorreoSmtpAdapter implements EnviadorCorreoPort {

    private static final Logger log = LoggerFactory.getLogger(CorreoSmtpAdapter.class);

    private final JavaMailSender mailSender;
    private final String remitente;
    private final ExecutorService ejecutor = Executors.newFixedThreadPool(2);

    public CorreoSmtpAdapter(JavaMailSender mailSender,
                              @Value("${MAIL_FROM:SGFA <no-reply@sgfa.local>}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviar(String destinatario, String asunto, String cuerpoHtml) {
        ejecutor.submit(() -> {
            try {
                MimeMessage mensaje = mailSender.createMimeMessage();
                MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, false, "UTF-8");
                ayudante.setFrom(remitente);
                ayudante.setTo(destinatario);
                ayudante.setSubject(asunto);
                ayudante.setText(cuerpoHtml, true);
                mailSender.send(mensaje);
            } catch (Exception e) {
                log.error("No se pudo enviar el correo \"{}\"", asunto, e);
            }
        });
    }

    @PreDestroy
    void cerrar() {
        ejecutor.shutdown();
    }
}