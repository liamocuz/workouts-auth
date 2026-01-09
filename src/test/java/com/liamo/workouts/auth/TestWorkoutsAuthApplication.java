package com.liamo.workouts.auth;

import org.springframework.boot.SpringApplication;

public class TestWorkoutsAuthApplication {

    public static void main(String[] args) {
        SpringApplication.from(WorkoutsAuthApplication::main).with(PostgreSQLTestcontainer.class).run(args);
    }

}
