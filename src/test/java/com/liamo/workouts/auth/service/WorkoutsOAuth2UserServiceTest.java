package com.liamo.workouts.auth.service;

import com.liamo.workouts.auth.WorkoutsTestUtil;
import com.liamo.workouts.auth.model.AuthProvider;
import com.liamo.workouts.auth.model.WorkoutsClaims;
import com.liamo.workouts.auth.model.entity.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutsOAuth2UserServiceTest {

    @Mock
    private UserInfoService userInfoService;

    private WorkoutsOAuth2UserService workoutsOAuth2UserService;

    @BeforeEach
    void setUp() {
        workoutsOAuth2UserService = spy(new WorkoutsOAuth2UserService(userInfoService));
    }

    @Test
    void loadUser_whenFacebookUser_thenExtractsAttributesAndCreatesUserInfo() {
        // Arrange
        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId("facebook")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .authorizationUri("https://www.facebook.com/v23.0/dialog/oauth")
            .tokenUri("https://graph.facebook.com/v23.0/oauth/access_token")
            .userInfoUri("https://graph.facebook.com/v20.0/me")
            .userNameAttributeName("id")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600)
        );

        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        // Mock the OAuth2 user returned by parent class
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "facebook-123");
        attributes.put("email", "user@facebook.com");
        attributes.put("first_name", "John");
        attributes.put("last_name", "Doe");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "id"
        );

        doReturn(mockOAuth2User).when(workoutsOAuth2UserService).loadUser(userRequest);

        // Mock UserInfo from database
        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.FACEBOOK)
            .sub("facebook-123")
            .email("user@facebook.com")
            .givenName("John")
            .familyName("Doe")
            .build();

        when(userInfoService.findOrRegisterOAuthUser(eq(AuthProvider.FACEBOOK), any(WorkoutsClaims.class)))
            .thenReturn(mockUserInfo);

        // Act
        OAuth2User result = workoutsOAuth2UserService.loadUser(userRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAttributes()).containsKey("id");
        assertThat(result.getName()).isEqualTo("facebook-123");
    }

    @Test
    void loadUser_whenCalled_thenInvokesUserInfoService() {
        // Arrange
        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId("facebook")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .authorizationUri("https://www.facebook.com/v23.0/dialog/oauth")
            .tokenUri("https://graph.facebook.com/v23.0/oauth/access_token")
            .userInfoUri("https://graph.facebook.com/v20.0/me")
            .userNameAttributeName("id")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600)
        );

        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "facebook-456");
        attributes.put("email", "test@facebook.com");
        attributes.put("first_name", "Jane");
        attributes.put("last_name", "Smith");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "id"
        );

        doReturn(mockOAuth2User).when(workoutsOAuth2UserService).loadUser(userRequest);

        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.FACEBOOK)
            .sub("facebook-456")
            .email("test@facebook.com")
            .givenName("Jane")
            .familyName("Smith")
            .build();

        ArgumentCaptor<WorkoutsClaims> claimsCaptor = ArgumentCaptor.forClass(WorkoutsClaims.class);
        when(userInfoService.findOrRegisterOAuthUser(eq(AuthProvider.FACEBOOK), claimsCaptor.capture()))
            .thenReturn(mockUserInfo);

        // Act
        workoutsOAuth2UserService.loadUser(userRequest);

        // Assert - verify that userInfoService was called
        verify(userInfoService, times(1)).findOrRegisterOAuthUser(eq(AuthProvider.FACEBOOK), any(WorkoutsClaims.class));
    }

    @Test
    void loadUser_whenProviderIsFacebook_thenParsesProviderCorrectly() {
        // Arrange
        ClientRegistration clientRegistration = ClientRegistration
            .withRegistrationId("facebook")
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .authorizationUri("https://www.facebook.com/v23.0/dialog/oauth")
            .tokenUri("https://graph.facebook.com/v23.0/oauth/access_token")
            .userInfoUri("https://graph.facebook.com/v20.0/me")
            .userNameAttributeName("id")
            .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600)
        );

        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "fb-789");
        attributes.put("email", "provider@test.com");
        attributes.put("first_name", "Test");
        attributes.put("last_name", "User");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "id"
        );

        doReturn(mockOAuth2User).when(workoutsOAuth2UserService).loadUser(userRequest);

        UserInfo mockUserInfo = WorkoutsTestUtil.getOAuth2UserInfoBuilder()
            .provider(AuthProvider.FACEBOOK)
            .sub("fb-789")
            .build();

        ArgumentCaptor<AuthProvider> providerCaptor = ArgumentCaptor.forClass(AuthProvider.class);
        when(userInfoService.findOrRegisterOAuthUser(providerCaptor.capture(), any(WorkoutsClaims.class)))
            .thenReturn(mockUserInfo);

        // Act
        workoutsOAuth2UserService.loadUser(userRequest);

        // Assert - verify AuthProvider.FACEBOOK was used
        verify(userInfoService).findOrRegisterOAuthUser(eq(AuthProvider.FACEBOOK), any(WorkoutsClaims.class));
        assertThat(providerCaptor.getValue()).isEqualTo(AuthProvider.FACEBOOK);
    }
}
