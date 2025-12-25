package com.liamo.workouts.auth.security;

import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.service.UserInfoService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles authentication success and failure events.
 */
@Component
public class AuthenticationEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventHandler.class);

    private final UserInfoService userInfoService;
    private final MeterRegistry meterRegistry;

    public AuthenticationEventHandler(UserInfoService userInfoService, MeterRegistry meterRegistry) {
        this.userInfoService = userInfoService;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Handle successful interactive authentication events. Updates last login timestamp and records metrics.
     */
    @EventListener
    @Transactional
    public void handleSuccess(InteractiveAuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        logger.debug("Interactive auth success: authType={}, principalType={}",
            auth.getClass().getName(),
            auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");

        resolvePublicId(auth).ifPresentOrElse(publicId -> {
            int numRowsUpdated = userInfoService.updateLastLoginByPublicId(publicId);
            if (numRowsUpdated == 0) {
                logger.warn("LastLogin update affected 0 rows for publicId={}", publicId);
            } else {
                logger.debug("LastLogin updated for publicId={}, numRows={}", publicId, numRowsUpdated);
            }
        }, () -> logger.warn("Could not extract publicId from authentication type={}, principalClass={}",
            auth.getClass().getName(),
            auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null"));

        resolveAuthProvider(auth).ifPresent(authProvider -> {
                Tags successTags = Tags.of("result", "success", "provider", authProvider.name());
                meterRegistry.counter("auth.login.count", successTags).increment();
            }
        );
    }

    @EventListener
    public void handleFailure(AbstractAuthenticationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        logger.debug("Auth failure: authType={}, principalType={}",
            auth.getClass().getName(),
            auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");

        AuthenticationException exception = event.getException();
        String error = exception.getClass().getSimpleName();

        resolveAuthProvider(auth).ifPresent(authProvider -> {
                Tags failureTags = Tags.of("result", "failure", "provider", authProvider.name(),
                    "error", error
                );
                meterRegistry.counter("auth.login.count", failureTags).increment();
            }
        );
    }

    /**
     * Extract the publicId UUID from the Authentication.
     * - Local form login: userDetails.getUsername() holds the publicId
     * - Federated (OIDC/OAuth2): principal.getName() holds the publicId
     */
    private Optional<UUID> resolvePublicId(Authentication auth) {
        // Local form login
        if (auth instanceof UsernamePasswordAuthenticationToken upat) {
            Object principal = upat.getPrincipal();
            if (principal instanceof UserDetails ud) {
                return parseUuid(ud.getUsername());
            }
        }

        // Federated login (OIDC / OAuth2)
        if (auth instanceof OAuth2AuthenticationToken) {
            Object principal = auth.getPrincipal();
            if (principal instanceof OidcUser oidc) {
                return parseUuid(oidc.getName());
            }
            if (principal instanceof OAuth2User oAuth2User) {
                return parseUuid(oAuth2User.getName());
            }
        }

        return Optional.empty();
    }

    private Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (Exception e) {
            logger.debug("Value is not a valid UUID: {}", value);
            return Optional.empty();
        }
    }

    private Optional<AuthProvider> resolveAuthProvider(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken oauth2Token) {
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId().toUpperCase();
            try {
                return Optional.of(AuthProvider.valueOf(registrationId));
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown AuthProvider: {}", registrationId);
                return Optional.empty();
            }
        } else if (auth instanceof UsernamePasswordAuthenticationToken) {
            return Optional.of(AuthProvider.LOCAL);
        }

        return Optional.empty();
    }
}