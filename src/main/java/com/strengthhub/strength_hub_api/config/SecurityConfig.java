package com.strengthhub.strength_hub_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Allow Swagger UI and API docs
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // Allow H2 console if you're using it for development
                        .requestMatchers("/h2-console/**").permitAll()
                        // Add your public endpoints here (like registration, login)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Require authentication for all other requests
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf
                        // Disable CSRF for H2 console and API endpoints
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                );

        return http.build();
    }
}