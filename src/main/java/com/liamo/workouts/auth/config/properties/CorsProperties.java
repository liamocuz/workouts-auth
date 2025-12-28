package com.liamo.workouts.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
    List<String> allowedOrigins,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    Boolean allowCredentials
) {
    public CorsProperties {
        // Set defaults if null
        if (allowedOrigins == null) {
            allowedOrigins = List.of("http://localhost:9090");
        }
        if (allowedMethods == null) {
            allowedMethods = List.of("*");
        }
        if (allowedHeaders == null) {
            allowedHeaders = List.of("*");
        }
        if (allowCredentials == null) {
            allowCredentials = true;
        }
    }
}
