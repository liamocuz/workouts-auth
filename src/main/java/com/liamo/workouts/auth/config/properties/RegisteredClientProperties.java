package com.liamo.workouts.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * A record to hold registered client properties.
 *
 * @param reactBffClientId     The client ID for the React BFF client.
 * @param reactBffClientSecret The client secret for the React BFF client.
 */
@ConfigurationProperties(prefix = "registered-client")
public record RegisteredClientProperties(
    String reactBffClientId,
    String reactBffClientSecret
) {
}
