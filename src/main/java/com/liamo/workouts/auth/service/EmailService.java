package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.model.entity.UserVerification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * A service to manage sending emails to users. Specifically for the
 * {@link UserVerification} flow.
 *
 */
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // TODO: Formalize this with configuration
    public void sendRegistrationEmail(String email, UUID verificationId) {
        String verificationLink = "http://localhost:8080/verify?token=" + verificationId.toString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Registration Email");
        message.setText(
                """
                Welcome to Auth.
                Verify your account: %s
                """.formatted(verificationLink)
        );

        mailSender.send(message);
    }
}
