package com.liamo.workouts.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendRegistrationEmail_thenSend() {
        String email = "bob@example.com";
        UUID token = UUID.randomUUID();

        emailService.sendRegistrationEmail(email, token);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Registration Email");
        mailMessage.setText(
                """
                Welcome to Auth.
                Verify your account: http://localhost:8080/verify?token=%s
                """.formatted(token.toString())
        );

        verify(mailSender, times(1)).send(mailMessage);
    }
}