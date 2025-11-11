package com.example.dependencies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DependenciesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DependenciesApplication.class, args);
    }

    @RestController
    public static class HealthController {

        @GetMapping("/")
        public String home() {
            return "Dependencies Service is running!";
        }

        @GetMapping("/health")
        public String health() {
            return "OK";
        }
    }
}
