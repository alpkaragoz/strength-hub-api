package com.strengthhub.strength_hub_api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test configuration class that provides beans specifically for testing
 */
@TestConfiguration
@ActiveProfiles("test")
public class TestConfig {

    /**
     * Provides a password encoder for testing
     * Uses a lighter encoder for faster test execution
     */
    @Bean
    @Primary
    @Profile("test")
    public PasswordEncoder testPasswordEncoder() {
        // Use a lighter encoder for faster test execution
        return new BCryptPasswordEncoder(4); // Lower rounds for testing
    }
}