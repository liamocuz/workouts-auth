package com.liamo.workoutsauth;

import org.springframework.boot.SpringApplication;

public class TestWorkoutsAuthApplication {

    public static void main(String[] args) {
        SpringApplication.from(WorkoutsAuthApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
