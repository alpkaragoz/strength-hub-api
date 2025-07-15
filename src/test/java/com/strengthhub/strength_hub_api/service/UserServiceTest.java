package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.request.coach.CoachRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.request.user.UserRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.request.user.UserUpdateRequest;
import com.strengthhub.strength_hub_api.dto.response.user.UserResponse;
import com.strengthhub.strength_hub_api.enums.UserType;
import com.strengthhub.strength_hub_api.exception.user.UserAlreadyExistsException;
import com.strengthhub.strength_hub_api.exception.user.UserNotFoundException;
import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.Lifter;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.LifterRepository;
import com.strengthhub.strength_hub_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LifterRepository lifterRepository;

    @Mock
    private CoachService coachService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationRequest validRegistrationRequest;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = User.builder()
                .userId(testUserId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encoded_password")
                .firstName("Test")
                .lastName("User")
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        validRegistrationRequest = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    @DisplayName("Should register user successfully with valid request")
    void registerUser_WithValidRequest_ShouldCreateUserAndLifterProfile() {
        // Given
        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded_password");

        // Mock the save to return a user with lifter profile set
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUserId(testUserId); // Set the ID as if it was saved
            return savedUser;
        });

        given(lifterRepository.save(any(Lifter.class))).willAnswer(invocation -> {
            Lifter lifter = invocation.getArgument(0);
            // Set up bidirectional relationship
            User user = lifter.getApp_user();
            user.setLifterProfile(lifter);
            return lifter;
        });

        // When
        UserResponse result = userService.registerUser(validRegistrationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getIsAdmin()).isFalse();
        assertThat(result.getRoles()).containsExactly(UserType.LIFTER);

        then(userRepository).should().save(any(User.class));
        then(lifterRepository).should().save(any(Lifter.class));
        then(coachService).should(never()).createCoach(any(UUID.class), any(CoachRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should register user with coach code successfully")
    void registerUser_WithCoachCode_ShouldCreateUserLifterAndCoach() {
        // Given
        validRegistrationRequest.setCoachCode("COACH123");

        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(lifterRepository.save(any(Lifter.class))).willReturn(new Lifter());

        // When
        UserResponse result = userService.registerUser(validRegistrationRequest);

        // Then
        assertThat(result).isNotNull();
        then(coachService).should().createCoach(any(UUID.class), any(CoachRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void registerUser_WithExistingUsername_ShouldThrowUserAlreadyExistsException() {
        // Given
        given(userRepository.existsByUsername("testuser")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(validRegistrationRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");

        then(userRepository).should(never()).save(any(User.class));
        then(lifterRepository).should(never()).save(any(Lifter.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void registerUser_WithExistingEmail_ShouldThrowUserAlreadyExistsException() {
        // Given
        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(validRegistrationRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email");

        then(userRepository).should(never()).save(any(User.class));
        then(lifterRepository).should(never()).save(any(Lifter.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getUserById_WithValidId_ShouldReturnUser() {
        // Given
        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUserById(testUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void getUserById_WithInvalidId_ShouldThrowUserNotFoundException() {
        // Given
        UUID invalidId = UUID.randomUUID();
        given(userRepository.findById(invalidId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(invalidId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(invalidId.toString());
    }

    @Test
    @DisplayName("Should get all users successfully")
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        User user2 = User.builder()
                .userId(UUID.randomUUID())
                .username("testuser2")
                .email("test2@example.com")
                .passwordHash("encoded_password")
                .firstName("Test2")
                .lastName("User2")
                .isAdmin(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<User> users = Arrays.asList(testUser, user2);
        given(userRepository.findAll()).willReturn(users);

        // When
        List<UserResponse> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(1).getUsername()).isEqualTo("testuser2");
        assertThat(result.get(1).getIsAdmin()).isTrue();
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_WithValidRequest_ShouldUpdateUser() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .password("newpassword123")
                .build();

        User updatedUser = User.builder()
                .userId(testUserId)
                .username("updateduser")
                .email("updated@example.com")
                .passwordHash("new_encoded_password")
                .firstName("Updated")
                .lastName("User")
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(userRepository.existsByUsername("updateduser")).willReturn(false);
        given(userRepository.existsByEmail("updated@example.com")).willReturn(false);
        given(passwordEncoder.encode("newpassword123")).willReturn("new_encoded_password");
        given(userRepository.save(any(User.class))).willReturn(updatedUser);

        // When
        UserResponse result = userService.updateUser(testUserId, updateRequest);

        // Then
        assertThat(result.getUsername()).isEqualTo("updateduser");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("User");
    }

    @Test
    @DisplayName("Should not update if username already exists")
    void updateUser_WithExistingUsername_ShouldThrowException() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .username("existinguser")
                .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(userRepository.existsByUsername("existinguser")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(testUserId, updateRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username");
    }

    @Test
    @DisplayName("Should allow updating to same username")
    void updateUser_WithSameUsername_ShouldNotCheckExistence() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .username("testuser") // Same username
                .firstName("Updated")
                .build();

        User updatedUser = User.builder()
                .userId(testUserId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encoded_password")
                .firstName("Updated")
                .lastName("User")
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willReturn(updatedUser);

        // When
        UserResponse result = userService.updateUser(testUserId, updateRequest);

        // Then
        assertThat(result.getFirstName()).isEqualTo("Updated");
        then(userRepository).should(never()).existsByUsername(anyString());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Given
        given(userRepository.existsById(testUserId)).willReturn(true);

        // When
        userService.deleteUser(testUserId);

        // Then
        then(userRepository).should().deleteById(testUserId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void deleteUser_WithInvalidId_ShouldThrowUserNotFoundException() {
        // Given
        UUID invalidId = UUID.randomUUID();
        given(userRepository.existsById(invalidId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(invalidId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(invalidId.toString());

        then(userRepository).should(never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should get user roles correctly for lifter only")
    void getUserRoles_WithLifterOnly_ShouldReturnLifterRole() {
        // Given
        Lifter lifter = Lifter.builder().build();
        testUser.setLifterProfile(lifter);

        // When
        Set<UserType> roles = userService.getUserRoles(testUser);

        // Then
        assertThat(roles).containsExactly(UserType.LIFTER);
    }

    @Test
    @DisplayName("Should get user roles correctly for coach only")
    void getUserRoles_WithCoachOnly_ShouldReturnCoachRole() {
        // Given
        Coach coach = Coach.builder().build();
        testUser.setCoachProfile(coach);

        // When
        Set<UserType> roles = userService.getUserRoles(testUser);

        // Then
        assertThat(roles).containsExactly(UserType.COACH);
    }

    @Test
    @DisplayName("Should get user roles correctly for both lifter and coach")
    void getUserRoles_WithBothRoles_ShouldReturnBothRoles() {
        // Given
        Lifter lifter = Lifter.builder().build();
        Coach coach = Coach.builder().build();
        testUser.setLifterProfile(lifter);
        testUser.setCoachProfile(coach);

        // When
        Set<UserType> roles = userService.getUserRoles(testUser);

        // Then
        assertThat(roles).containsExactlyInAnyOrder(UserType.LIFTER, UserType.COACH);
    }

    @Test
    @DisplayName("Should update only provided fields")
    void updateUser_WithPartialRequest_ShouldUpdateOnlyProvidedFields() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("Updated")
                .build();

        User updatedUser = User.builder()
                .userId(testUserId)
                .username("testuser") // Same
                .email("test@example.com") // Same
                .passwordHash("encoded_password") // Same
                .firstName("Updated") // Changed
                .lastName("User") // Same
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willReturn(updatedUser);

        // When
        UserResponse result = userService.updateUser(testUserId, updateRequest);

        // Then
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getUsername()).isEqualTo("testuser"); // Unchanged
        assertThat(result.getEmail()).isEqualTo("test@example.com"); // Unchanged
    }
}