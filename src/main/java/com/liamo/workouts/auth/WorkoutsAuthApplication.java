package com.liamo.workouts.auth;

import com.liamo.workouts.auth.config.properties.RegisteredClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RegisteredClientProperties.class)
@SpringBootApplication
public class WorkoutsAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkoutsAuthApplication.class, args);
    }
}
