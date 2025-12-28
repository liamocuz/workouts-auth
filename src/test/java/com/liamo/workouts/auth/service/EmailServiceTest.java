package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.config.properties.EmailProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailProperties emailProperties;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendRegistrationEmail_thenSend() {
        String email = "bob@example.com";
        UUID token = UUID.randomUUID();

        when(emailProperties.baseUrl()).thenReturn("http://localhost:8080");
        when(emailProperties.fromAddress()).thenReturn("noreply@test.com");
        when(emailProperties.fromName()).thenReturn("Test App");
        
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendRegistrationEmail(email, token);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        assertNotNull(sentMessage);
        assertEquals(email, sentMessage.getTo()[0]);
        assertNotNull(sentMessage.getSubject());
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(token.toString()));
    }
}
