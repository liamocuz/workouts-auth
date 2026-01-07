package com.liamo.workouts.auth.security;

import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.entity.UserInfo;
import com.liamo.workouts.auth.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * This class handles filling out the JWT we return to the frontend with the related
 * {@link UserInfo}'s information.
 */
@Configuration
public class JwtConfig {
    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    private final UserInfoService userInfoService;

    public JwtConfig(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (!AuthorizationGrantType.AUTHORIZATION_CODE.equals(context.getAuthorizationGrantType())) {
                return;
            }

            Optional<UUID> publicId = resolvePublicId(context);
            if (publicId.isEmpty()) return;

            Optional<UserInfo> userInfo = userInfoService.findByPublicId(publicId.get());
            if (userInfo.isEmpty()) return;

            JwtClaimsSet.Builder claimsBuilder = context.getClaims();

            // We set the sub claim to our publicId always
            claimsBuilder.claim("sub", publicId.get().toString());

            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                logger.debug("Access token being customized");
                buildAccessTokenClaims(userInfo.get(), claimsBuilder);
            } else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                logger.debug("ID token being customized");
                buildIdTokenClaims(userInfo.get(), claimsBuilder);
            }

            logger.debug("Jwt token customizer claims: {}", claimsBuilder);
        };
    }

    private Optional<UUID> resolvePublicId(JwtEncodingContext context) {
        OAuth2Authorization authorization = context.getAuthorization();

        if (authorization != null) {
            try {
                return Optional.of(UUID.fromString(authorization.getPrincipalName()));
            } catch (Exception ignored) {
                logger.debug("Unable to parse publicId from authorization principalName: {}",
                    authorization.getPrincipalName());
            }
        }

        // Fallback
        Authentication auth = context.getPrincipal();
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

    private void buildAccessTokenClaims(UserInfo userInfo, JwtClaimsSet.Builder claimsBuilder) {
        Set<String> roles = getRolesStringArray(userInfo.getRoles());
        claimsBuilder.claim("roles", roles);
    }

    private Set<String> getRolesStringArray(Set<AuthRole> roles) {
        return roles
            .stream()
            .map(AuthRole::getRoles)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    private void buildIdTokenClaims(UserInfo userInfo, JwtClaimsSet.Builder claimsBuilder) {
        claimsBuilder.claim("email", userInfo.getEmail());
        claimsBuilder.claim("email_verified", userInfo.isEmailVerified());
        claimsBuilder.claim("given_name", userInfo.getGivenName());
        claimsBuilder.claim("family_name", userInfo.getFamilyName());
        claimsBuilder.claim("name", userInfo.getDisplayName());
    }
}
