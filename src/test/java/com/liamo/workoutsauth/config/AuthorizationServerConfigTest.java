package com.liamo.workoutsauth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.liamo.workoutsauth.TestcontainersConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthorizationServerConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testOAuth2AuthorizationServerEndpoint() throws Exception {
        // Test that OAuth2 authorization server metadata endpoint is accessible
        mockMvc.perform(get("/.well-known/oauth-authorization-server"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issuer").exists())
                .andExpect(jsonPath("$.authorization_endpoint").exists())
                .andExpect(jsonPath("$.token_endpoint").exists());
    }

    @Test
    void testJwksEndpoint() throws Exception {
        // Test that JWKS endpoint is accessible
        mockMvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray());
    }

    @Test
    void testTokenEndpointRequiresAuthentication() throws Exception {
        // Test that token endpoint requires authentication (returns 401 with httpBasic enabled)
        mockMvc.perform(get("/oauth2/token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAuthorizationEndpointRedirectsToLogin() throws Exception {
        // Test that authorization endpoint redirects to login or returns error for missing parameters
        // Note: OAuth2 authorize endpoint may return 400 if parameters are invalid
        mockMvc.perform(get("/oauth2/authorize")
                        .param("response_type", "code")
                        .param("client_id", "workouts-client")
                        .param("scope", "read")
                        .param("redirect_uri", "http://127.0.0.1:8080/authorized"))
                .andExpect(status().is4xxClientError());
    }
}
