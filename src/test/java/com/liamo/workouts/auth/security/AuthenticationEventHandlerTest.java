package com.liamo.workouts.auth.security;

import com.liamo.workouts.auth.service.UserInfoService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationEventHandlerTest {

    @Mock
    private UserInfoService userInfoService;
    @Mock
    private Counter counter;
    @Mock
    private MeterRegistry meterRegistry;
    @InjectMocks
    private AuthenticationEventHandler authenticationEventHandler;

    @BeforeEach
    void beforeEach() {
        when(meterRegistry.counter(anyString(), any(Tags.class))).thenReturn(counter);
        doNothing().when(counter).increment();
    }

    @Test
    void handleSuccess_whenUserDetailsPrincipal_thenUpdateLastLoginAndIncrementMetric() {
        // Arrange
        UUID publicId = UUID.randomUUID();
        InteractiveAuthenticationSuccessEvent event = mock(InteractiveAuthenticationSuccessEvent.class);
        UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn(publicId.toString());
        when(auth.getPrincipal()).thenReturn(principal);
        when(event.getAuthentication()).thenReturn(auth);

        when(userInfoService.updateLastLoginByPublicId(any(UUID.class))).thenReturn(1);

        // Act
        authenticationEventHandler.handleSuccess(event);

        // Assert
        verify(userInfoService).updateLastLoginByPublicId(publicId);
        verify(meterRegistry).counter(anyString(), any(Tags.class));
        verify(counter).increment();
    }

    @Test
    void handleSuccess_whenOidcUserPrincipal_thenUpdateLastLoginAndIncrementMetric() {
        // Arrange
        UUID publicId = UUID.randomUUID();

        InteractiveAuthenticationSuccessEvent event = mock(InteractiveAuthenticationSuccessEvent.class);
        OAuth2AuthenticationToken auth = mock(OAuth2AuthenticationToken.class);
        when(auth.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(event.getAuthentication()).thenReturn(auth);
        OidcUser principal = mock(OidcUser.class);
        when(principal.getName()).thenReturn(publicId.toString());
        when(auth.getPrincipal()).thenReturn(principal);

        when(userInfoService.updateLastLoginByPublicId(any(UUID.class))).thenReturn(1);

        // Act
        authenticationEventHandler.handleSuccess(event);

        // Assert
        verify(userInfoService).updateLastLoginByPublicId(publicId);
        verify(meterRegistry).counter(anyString(), any(Tags.class));
        verify(counter).increment();
    }

    @Test
    void handleSuccess_whenOAuth2UserPrincipal_thenUpdateLastLoginAndIncrementMetric() {
        // Arrange
        UUID publicId = UUID.randomUUID();

        InteractiveAuthenticationSuccessEvent event = mock(InteractiveAuthenticationSuccessEvent.class);
        OAuth2AuthenticationToken auth = mock(OAuth2AuthenticationToken.class);
        when(auth.getAuthorizedClientRegistrationId()).thenReturn("facebook");
        when(event.getAuthentication()).thenReturn(auth);
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getName()).thenReturn(publicId.toString());
        when(auth.getPrincipal()).thenReturn(principal);

        when(userInfoService.updateLastLoginByPublicId(any(UUID.class))).thenReturn(1);

        // Act
        authenticationEventHandler.handleSuccess(event);

        // Assert
        verify(userInfoService).updateLastLoginByPublicId(publicId);
        verify(meterRegistry).counter(anyString(), any(Tags.class));
        verify(counter).increment();
    }

    @Test
    void handleFailure_whenUsernamePasswordAuthenticationToken_thenIncrementFailureMetric() {
        // Arrange
        AbstractAuthenticationFailureEvent event = mock(AbstractAuthenticationFailureEvent.class);
        UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);
        when(event.getAuthentication()).thenReturn(auth);
        when(event.getException()).thenReturn(new AuthenticationServiceException("Error"));

        // Act
        authenticationEventHandler.handleFailure(event);

        // Assert
        verify(meterRegistry).counter(anyString(), any(Tags.class));
        verify(counter).increment();
    }

    @Test
    void handleFailure_whenOAuth2AuthenticationToken_thenIncrementFailureMetric() {
        // Arrange
        AbstractAuthenticationFailureEvent event = mock(AbstractAuthenticationFailureEvent.class);
        OAuth2AuthenticationToken auth = mock(OAuth2AuthenticationToken.class);
        when(auth.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(event.getAuthentication()).thenReturn(auth);
        when(event.getException()).thenReturn(new AuthenticationServiceException("Error"));

        // Act
        authenticationEventHandler.handleFailure(event);

        // Assert
        verify(meterRegistry).counter(anyString(), any(Tags.class));
        verify(counter).increment();
    }
}