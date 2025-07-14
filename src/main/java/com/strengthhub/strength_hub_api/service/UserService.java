package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.request.CoachRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.request.UserRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.request.UserUpdateRequest;
import com.strengthhub.strength_hub_api.dto.response.UserResponse;
import com.strengthhub.strength_hub_api.enums.UserType;
import com.strengthhub.strength_hub_api.exception.user.UserAlreadyExistsException;
import com.strengthhub.strength_hub_api.exception.user.UserNotFoundException;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.model.Lifter;
import com.strengthhub.strength_hub_api.repository.UserRepository;
import com.strengthhub.strength_hub_api.repository.LifterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final LifterRepository lifterRepository;
    private final CoachService coachService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering user with username: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("username", request.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("email", request.getEmail());
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // Automatically create lifter profile (mandatory)
        Lifter lifter = Lifter.builder()
                .lifterId(savedUser.getUserId())
                .app_user(savedUser)
                .build();
        lifterRepository.save(lifter);

        // Process coach code if provided
        if (request.getCoachCode() != null && !request.getCoachCode().trim().isEmpty()) {
            CoachRegistrationRequest req = CoachRegistrationRequest.builder()
                    .coachCode(request.getCoachCode())
                    .build();
//            coachService.createCoach(savedUser.getUserId(), req);
        }

        log.info("User registered with id: {}", savedUser.getUserId());
        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        log.info("Fetching user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UserAlreadyExistsException("username", request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated with id: {}", updatedUser.getUserId());

        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user with id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(String.valueOf(userId));
        }

        userRepository.deleteById(userId);
        log.info("User deleted with id: {}", userId);
    }


    public Set<UserType> getUserRoles (User user) {
        Set<UserType> roles = new HashSet<>();

        if (user.isLifter()) {
            roles.add(UserType.LIFTER);
        }

        if (user.isCoach()) {
            roles.add(UserType.COACH);
        }

        return roles;
    }

    private UserResponse mapToResponse(User user) {
        Set<UserType> roles = getUserRoles(user);

        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isAdmin(user.getIsAdmin())
                .createdAt(user.getCreatedAt())
                .roles(roles)
                .build();
    }
}