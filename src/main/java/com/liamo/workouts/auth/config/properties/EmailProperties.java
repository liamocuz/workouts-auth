package com.liamo.workouts.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
    String baseUrl,
    String fromAddress,
    String fromName
) {
    public EmailProperties {
        // Set defaults if null
        if (baseUrl == null) {
            baseUrl = "http://localhost:8080";
        }
        if (fromAddress == null) {
            fromAddress = "noreply@workouts-auth.com";
        }
        if (fromName == null) {
            fromName = "Workouts Auth";
        }
    }
}
