package com.liamo.workoutsauth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.liamo.workoutsauth.TestcontainersConfiguration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHealthEndpointIsPublic() throws Exception {
        // Test that health endpoint is accessible without authentication
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointRequiresAuthentication() throws Exception {
        // Test that other endpoints require authentication (returns 401 with httpBasic enabled)
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAuthenticatedAccessToProtectedEndpoint() throws Exception {
        // Test that authenticated users can access protected endpoints
        // Note: With form login enabled, httpBasic may still redirect
        mockMvc.perform(get("/actuator/info")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidCredentials() throws Exception {
        // Test that invalid credentials are rejected (returns 401 with httpBasic enabled)
        mockMvc.perform(get("/actuator/info")
                        .with(httpBasic("user", "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }
}
