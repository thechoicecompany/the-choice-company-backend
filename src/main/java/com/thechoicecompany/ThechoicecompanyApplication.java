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
            // Always validate — staging/UAT environments that aren't tagged "prod"
            // must still have a real secret, not a placeholder.
            if ("dev".equals(activeProfile) || "test".equals(activeProfile)) {
                // Allow weak secrets in local dev/test only
                if (jwtSecret.isBlank()) {
                    throw new IllegalStateException(
                        "[TCC] JWT_SECRET is not set. Add it to application-" + activeProfile + ".properties."
                    );
                }
            } else {
                // Staging, prod, or any other profile: enforce minimum strength
                if (jwtSecret.isBlank() || jwtSecret.length() < 64) {
                    throw new IllegalStateException(
                        "[TCC] JWT_SECRET is missing or too short for profile '" + activeProfile + "'. " +
                        "Minimum 64 characters required. Generate with: openssl rand -base64 64"
                    );
                }
            }
        };
    }

}  
