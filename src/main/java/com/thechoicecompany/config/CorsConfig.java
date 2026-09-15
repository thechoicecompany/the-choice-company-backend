package com.thechoicecompany.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
     registry.addMapping("/api/**")
    .allowedOriginPatterns(
        "http://localhost:3000",
        "https://thechoicecompany.in",
        "https://www.thechoicecompany.in",
        "https://*.netlify.app",
        "https://6aa64d3a60fee800081025e9--whimsical-fox-2ba37f.netlify.app/"  // ← covers all Netlify preview deploys
    )
    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    .allowedHeaders("*")
    .exposedHeaders("Authorization")
    .allowCredentials(true)
    .maxAge(3600);
    }
}
