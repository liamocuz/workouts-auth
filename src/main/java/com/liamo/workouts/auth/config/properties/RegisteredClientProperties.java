package com.liamo.workouts.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "registered-client")
public record RegisteredClientProperties(
    String reactBffClientId,
    String reactBffClientSecret,
    String reactBffRedirectUri,
    String reactBffPostLogoutRedirectUri
) {
}
