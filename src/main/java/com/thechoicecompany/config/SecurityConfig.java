package com.thechoicecompany.config;

import com.thechoicecompany.security.JwtAuthFilter;
import com.thechoicecompany.security.RateLimitFilter;
import com.thechoicecompany.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // =========================================================
                // PUBLIC AUTHENTICATION
                // =========================================================

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/login"
                ).permitAll()


                // =========================================================
                // DEV ONLY BOOTSTRAP
                // =========================================================
                // Used only to create the FIRST SUPER_ADMIN.
                //
                // BootstrapController itself has:
                // @Profile("dev")
                //
                // Therefore this endpoint does not exist in production.
                // =========================================================

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/dev/bootstrap/admin"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/catalogue/request"
                ).permitAll()

                // =========================================================
                // PUBLIC WEBSITE APIs
                // =========================================================
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/api/sample-products/**").permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/inquiries"
                ).permitAll()
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/contact"
                ).permitAll()

                .requestMatchers(HttpMethod.GET, "/api/orders/track").permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/demo-orders"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/newsletter/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/products/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/blog/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/gallery/**"
                ).permitAll()

                .requestMatchers("/files/**").permitAll()


                // =========================================================
                // SWAGGER / ACTUATOR
                // =========================================================

                .requestMatchers(
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/actuator/health"
                ).permitAll()


                // =========================================================
                // EVERYTHING ELSE
                // =========================================================

                .anyRequest().authenticated()
            )

            .authenticationProvider(authenticationProvider())

            // Rate limiting runs FIRST — rejects abusive requests before
            // JWT parsing/auth logic spends any CPU on them.
            .addFilterBefore(
                rateLimitFilter,
                UsernamePasswordAuthenticationFilter.class
            )

            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }


    // =========================================================
    // AUTHENTICATION PROVIDER
    // =========================================================

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }


    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder(10);
    }
}