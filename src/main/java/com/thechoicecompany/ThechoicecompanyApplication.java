package com.thechoicecompany;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The Choice Company — Spring Boot 3.3.x Backend
 * Java 21 | PostgreSQL 18 | JWT Auth | REST API
 *
 * Entry point for the application.
 * @EnableJpaAuditing  → enables @CreatedDate and @LastModifiedDate on entities
 * @EnableScheduling   → enables @Scheduled jobs (follow-up reminders, reports)
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class ThechoicecompanyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThechoicecompanyApplication.class, args);
    }  

    @Bean
    ApplicationRunner validateSecrets(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${spring.profiles.active:dev}") String activeProfile
    ) {
        return args -> {
            if ("prod".equals(activeProfile)) {
                if (jwtSecret.isBlank() || jwtSecret.length() < 32) {
                    throw new IllegalStateException(
                        "[TCC] JWT_SECRET is missing or too short. " +
                        "Set a strong secret (min 32 chars) in your prod environment."
                    );
                }
            }
        };
    }

}  
