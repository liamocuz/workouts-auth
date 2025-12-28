package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.config.properties.EmailProperties;
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
    private final EmailProperties emailProperties;

    public EmailService(JavaMailSender mailSender, EmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
    }

    /**
     * Sends a registration verification email to the user.
     *
     * @param email The user's email address
     * @param verificationId The verification token UUID
     */
    public void sendRegistrationEmail(String email, UUID verificationId) {
        String verificationLink = emailProperties.baseUrl() + "/verify?token=" + verificationId.toString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.fromAddress());
        message.setTo(email);
        message.setSubject("Welcome to Workouts - Verify Your Email");
        message.setText(
                """
                Welcome to Workouts Auth!
                
                Please verify your email address by clicking the link below:
                %s
                
                This link will expire in 24 hours.
                
                If you didn't create an account, you can safely ignore this email.
                
                Best regards,
                %s
                """.formatted(verificationLink, emailProperties.fromName())
        );

        mailSender.send(message);
    }
}
