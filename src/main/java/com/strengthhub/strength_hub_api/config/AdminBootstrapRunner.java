package com.strengthhub.strength_hub_api.config;

import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.first-name}")
    private String adminFirstName;

    @Value("${app.admin.last-name}")
    private String adminLastName;

    @Override
    public void run(String... args) throws Exception {
        // Check if any admin user exists
        if (userRepository.findAdminUsers().isEmpty()) {
            createAdminUser();
        }
    }

    private void createAdminUser() {
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            log.warn("No admin password provided. Skipping admin user creation.");
            log.warn("Set app.admin.password environment variable or application property to create admin user.");
            return;
        }

        try {
            // Check if username or email already exists
            if (userRepository.existsByUsername(adminUsername)) {
                log.warn("Username '{}' already exists. Skipping admin user creation.", adminUsername);
                return;
            }

            if (userRepository.existsByEmail(adminEmail)) {
                log.warn("Email '{}' already exists. Skipping admin user creation.", adminEmail);
                return;
            }

            User adminUser = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .isAdmin(true)
                    .build();

            userRepository.save(adminUser);
            log.info("Admin user created successfully with username: {}", adminUsername);

        } catch (Exception e) {
            log.error("Failed to create admin user: {}", e.getMessage());
        }
    }
}