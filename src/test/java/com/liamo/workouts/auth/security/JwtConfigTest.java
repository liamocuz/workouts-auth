package com.liamo.workouts.auth.security;

import com.liamo.workouts.auth.WorkoutsTestUtil;
import com.liamo.workouts.auth.model.AuthRole;
import com.liamo.workouts.auth.model.entity.UserInfo;
import com.liamo.workouts.auth.service.UserInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtConfigTest {

    @Mock
    UserInfoService userInfoService;

    @InjectMocks
    JwtConfig jwtConfig;

    @Test
    void tokenCustomizer_whenAccessToken_thenSetRoles() {
        // Arrange
        UserInfo userInfo = WorkoutsTestUtil.getLocalUserInfoBuilder().build();
        UUID publicId = userInfo.getPublicId();

        when(userInfoService.findByPublicId(publicId)).thenReturn(Optional.of(userInfo));

        JwtEncodingContext context = mock(JwtEncodingContext.class);
        OAuth2Authorization authorization = mock(OAuth2Authorization.class);
        when(authorization.getPrincipalName()).thenReturn(WorkoutsTestUtil.PUBLIC_ID.toString());
        when(context.getAuthorization()).thenReturn(authorization);
        when(context.getTokenType()).thenReturn(OAuth2TokenType.ACCESS_TOKEN);

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
        when(context.getClaims()).thenReturn(claimsBuilder);

        // Act
        jwtConfig.tokenCustomizer().customize(context);

        // Assert
        JwtClaimsSet claims = claimsBuilder.build();
        String subClaim = claims.getClaim("sub");
        assertEquals(publicId.toString(), subClaim);

        Set<String> claimsRoles = claims.getClaim("roles");
        assertNotNull(claimsRoles);
        assertFalse(claimsRoles.isEmpty());

        Set<String> userInfoRoles = userInfo
            .getRoles()
            .stream()
            .map(AuthRole::getRoles)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        assertThat(claimsRoles).containsExactlyElementsOf(userInfoRoles);
    }

    @Test
    void tokenCustomizer_whenIdToken_thenSetClaims() {
        // Arrange
        UserInfo userInfo = WorkoutsTestUtil.getLocalUserInfoBuilder().build();
        UUID publicId = userInfo.getPublicId();

        when(userInfoService.findByPublicId(publicId)).thenReturn(Optional.of(userInfo));

        JwtEncodingContext context = mock(JwtEncodingContext.class);
        OAuth2Authorization authorization = mock(OAuth2Authorization.class);
        when(authorization.getPrincipalName()).thenReturn(WorkoutsTestUtil.PUBLIC_ID.toString());
        when(context.getAuthorization()).thenReturn(authorization);
        when(context.getTokenType()).thenReturn(new OAuth2TokenType(OidcParameterNames.ID_TOKEN));


        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
        when(context.getClaims()).thenReturn(claimsBuilder);

        // Act
        jwtConfig.tokenCustomizer().customize(context);

        // Assert
        JwtClaimsSet claims = claimsBuilder.build();
        String subClaim = claims.getClaim("sub");
        assertEquals(publicId.toString(), subClaim);

        assertEquals(claims.getClaim("email"), userInfo.getEmail());
        assertEquals(claims.getClaim("email_verified"), userInfo.isEmailVerified());
        assertEquals(claims.getClaim("given_name"), userInfo.getGivenName());
        assertEquals(claims.getClaim("family_name"), userInfo.getFamilyName());
        assertEquals(claims.getClaim("name"), userInfo.getGivenName());
    }
}
