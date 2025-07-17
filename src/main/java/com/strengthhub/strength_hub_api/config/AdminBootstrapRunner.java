package com.strengthhub.strength_hub_api.config;

import com.strengthhub.strength_hub_api.dto.request.coach.CoachRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.request.coach.CoachUpdateRequest;
import com.strengthhub.strength_hub_api.dto.request.user.UserRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.response.user.UserResponse;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.UserRepository;
import com.strengthhub.strength_hub_api.service.CoachCodeService;
import com.strengthhub.strength_hub_api.service.CoachService;
import com.strengthhub.strength_hub_api.service.UserService;
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
    private final UserService userService;
    private final CoachCodeService coachCodeService;
    private final CoachService coachService;

    // Admin user properties
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

    // Demo lifter properties
    @Value("${app.demo.lifter.username:demo-lifter}")
    private String lifterUsername;

    @Value("${app.demo.lifter.email:lifter@strengthhub.com}")
    private String lifterEmail;

    @Value("${app.demo.lifter.password:}")
    private String lifterPassword;

    @Value("${app.demo.lifter.first-name:Demo}")
    private String lifterFirstName;

    @Value("${app.demo.lifter.last-name:Lifter}")
    private String lifterLastName;

    // Demo coach properties
    @Value("${app.demo.coach.username:demo-coach}")
    private String coachUsername;

    @Value("${app.demo.coach.email:coach@strengthhub.com}")
    private String coachEmail;

    @Value("${app.demo.coach.password:}")
    private String coachPassword;

    @Value("${app.demo.coach.first-name:Demo}")
    private String coachFirstName;

    @Value("${app.demo.coach.last-name:Coach}")
    private String coachLastName;

    @Value("${app.demo.coach.bio:Experienced powerlifting coach with 10+ years of experience}")
    private String coachBio;

    @Value("${app.demo.coach.certifications:USAPL Certified, NSCA-CSCS}")
    private String coachCertifications;

    @Value("${app.demo.enabled:false}")
    private boolean demoUsersEnabled;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if no admin exists
        if (userRepository.findAdminUsers().isEmpty()) {
            createAdminUser();
        }

        // Create demo users if enabled and passwords are provided
        if (demoUsersEnabled) {
            createDemoUsers();
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

    private void createDemoUsers() {
        log.info("Demo users enabled. Creating demo lifter and coach users...");

        try {
            createDemoLifter();
            createDemoCoach();
        } catch (Exception e) {
            log.error("Failed to create demo users: {}", e.getMessage());
        }
    }

    private void createDemoLifter() {
        if (lifterPassword == null || lifterPassword.trim().isEmpty()) {
            log.warn("No lifter password provided. Skipping demo lifter creation.");
            return;
        }

        try {
            // Check if lifter already exists
            if (userRepository.existsByUsername(lifterUsername)) {
                log.info("Demo lifter user '{}' already exists. Skipping creation.", lifterUsername);
                return;
            }

            if (userRepository.existsByEmail(lifterEmail)) {
                log.info("Demo lifter email '{}' already exists. Skipping creation.", lifterEmail);
                return;
            }

            UserRegistrationRequest lifterRequest = UserRegistrationRequest.builder()
                    .username(lifterUsername)
                    .email(lifterEmail)
                    .password(lifterPassword)
                    .firstName(lifterFirstName)
                    .lastName(lifterLastName)
                    .build();

            userService.registerUser(lifterRequest);
            log.info("Demo lifter user created successfully with username: {}", lifterUsername);

        } catch (Exception e) {
            log.error("Failed to create demo lifter user: {}", e.getMessage());
        }
    }

    private void createDemoCoach() {
        if (coachPassword == null || coachPassword.trim().isEmpty()) {
            log.warn("No coach password provided. Skipping demo coach creation.");
            return;
        }

        try {
            // Check if coach already exists
            if (userRepository.existsByUsername(coachUsername)) {
                log.info("Demo coach user '{}' already exists. Skipping creation.", coachUsername);
                return;
            }

            if (userRepository.existsByEmail(coachEmail)) {
                log.info("Demo coach email '{}' already exists. Skipping creation.", coachEmail);
                return;
            }

            // Generate a coach code for the demo coach
            String coachCode = coachCodeService.generateCoachCode().getCode();
            log.info("Generated coach code for demo coach: {}", coachCode);

            UserRegistrationRequest coachRequest = UserRegistrationRequest.builder()
                    .username(coachUsername)
                    .email(coachEmail)
                    .password(coachPassword)
                    .firstName(coachFirstName)
                    .lastName(coachLastName)
                    .coachCode(coachCode)
                    .build();

            UserResponse registeredUser = userService.registerUser(coachRequest);

            CoachUpdateRequest coachUpdateRequest = CoachUpdateRequest.builder()
                    .bio(coachBio)
                    .certifications(coachCertifications).build();
            coachService.updateCoach(registeredUser.getUserId(), coachUpdateRequest);

            log.info("Demo coach user created successfully with username: {}", coachUsername);
            log.info("Demo coach has both lifter and coach profiles");

        } catch (Exception e) {
            log.error("Failed to create demo coach user: {}", e.getMessage());
        }
    }
}