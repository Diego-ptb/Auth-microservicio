package com.sanosysalvos.authservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.admin-email:admin@sanosysalvos.cl}")
    private String adminEmail;

    @Async
    public void sendVetRequestNotification(String clinicName, String userEmail, String username) {
        if (mailSender == null) {
            log.warn("[EMAIL] Sin SMTP configurado — solicitud veterinaria de '{}' para clínica '{}'", username, clinicName);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(adminEmail);
            msg.setSubject("Nueva solicitud de veterinaria — " + clinicName);
            msg.setText(String.format(
                "El usuario '%s' (%s) solicita el rol de veterinaria para la clínica '%s'.%n%n" +
                "Aprueba o rechaza desde el panel de administración o via Swagger en /swagger-ui.",
                username, userEmail, clinicName
            ));
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("[EMAIL] Error al notificar solicitud veterinaria: {}", e.getMessage());
        }
    }

    @Async
    public void sendVetRequestApproved(String userEmail, String clinicName) {
        if (mailSender == null) {
            log.info("[EMAIL] Solicitud aprobada para clínica '{}' ({})", clinicName, userEmail);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(userEmail);
            msg.setSubject("¡Tu solicitud de veterinaria fue aprobada! — Sanos y Salvos");
            msg.setText(String.format(
                "Tu solicitud para registrar la clínica '%s' en Sanos y Salvos fue aprobada.%n%n" +
                "Ya puedes iniciar sesión y acceder al panel de veterinaria.",
                clinicName
            ));
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("[EMAIL] Error al notificar aprobación: {}", e.getMessage());
        }
    }

    @Async
    public void sendVetRequestRejected(String userEmail, String clinicName, String notes) {
        if (mailSender == null) {
            log.info("[EMAIL] Solicitud rechazada para clínica '{}' ({})", clinicName, userEmail);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(userEmail);
            msg.setSubject("Tu solicitud de veterinaria — Sanos y Salvos");
            msg.setText(String.format(
                "Tu solicitud para la clínica '%s' no fue aprobada.%n%nMotivo: %s%n%n" +
                "Puedes enviar una nueva solicitud desde tu perfil en Sanos y Salvos.",
                clinicName, notes != null ? notes : "Sin detalles adicionales"
            ));
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("[EMAIL] Error al notificar rechazo: {}", e.getMessage());
        }
    }
}
