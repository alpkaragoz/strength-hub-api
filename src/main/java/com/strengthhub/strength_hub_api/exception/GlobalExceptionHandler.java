package com.strengthhub.strength_hub_api.exception;

import com.strengthhub.strength_hub_api.dto.response.ErrorResponse;
import com.strengthhub.strength_hub_api.exception.auth.TokenRefreshException;
import com.strengthhub.strength_hub_api.exception.coach.CoachAlreadyExistsException;
import com.strengthhub.strength_hub_api.exception.coach.CoachNotFoundException;
import com.strengthhub.strength_hub_api.exception.coach.InvalidCoachAssignmentException;
import com.strengthhub.strength_hub_api.exception.coach.InvalidCoachCodeException;
import com.strengthhub.strength_hub_api.exception.common.ForbiddenAccessException;
import com.strengthhub.strength_hub_api.exception.connection.ConnectionRequestNotFoundException;
import com.strengthhub.strength_hub_api.exception.connection.DuplicateConnectionRequestException;
import com.strengthhub.strength_hub_api.exception.connection.InvalidConnectionRequestException;
import com.strengthhub.strength_hub_api.exception.lifter.LifterAlreadyExistsException;
import com.strengthhub.strength_hub_api.exception.lifter.LifterNotFoundException;
import com.strengthhub.strength_hub_api.exception.user.InvalidUserTypeException;
import com.strengthhub.strength_hub_api.exception.user.UserAlreadyExistsException;
import com.strengthhub.strength_hub_api.exception.user.UserNotFoundException;
import com.strengthhub.strength_hub_api.exception.workout.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ===== VALIDATION EXCEPTIONS =====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message(message)
                .build();

        log.warn("Constraint violation: {}", message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ===== AUTHENTICATION & AUTHORIZATION EXCEPTIONS =====

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Authentication Failed")
                .message(e.getMessage())
                .build();
        log.warn("Authentication failed: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Login Failed")
                .message(e.getMessage())
                .build();
        log.warn("Bad credentials: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Access Denied")
                .message("You don't have permission to access this resource")
                .build();
        log.warn("Access denied: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(ForbiddenAccessException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Unauthorized Access")
                .message(e.getMessage())
                .build();
        log.warn("Unauthorized access: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefreshException(TokenRefreshException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Token Refresh Failed")
                .message(e.getMessage())
                .build();
        log.warn("Token refresh failed: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // ===== USER EXCEPTIONS =====

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("User Not Found")
                .message(e.getMessage())
                .build();
        log.warn("User not found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("User Already Exists")
                .message(e.getMessage())
                .build();
        log.warn("User already exists: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidUserTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserTypeException(InvalidUserTypeException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid User Type")
                .message(e.getMessage())
                .build();
        log.warn("Invalid user type: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ===== COACH EXCEPTIONS =====

    @ExceptionHandler(CoachNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCoachNotFoundException(CoachNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Coach Not Found")
                .message(e.getMessage())
                .build();
        log.warn("Coach not found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CoachAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCoachAlreadyExistsException(CoachAlreadyExistsException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Coach Already Exists")
                .message(e.getMessage())
                .build();
        log.warn("Coach already exists: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCoachAssignmentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCoachAssignmentException(InvalidCoachAssignmentException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Coach Assignment")
                .message(e.getMessage())
                .build();
        log.warn("Invalid coach assignment: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCoachCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCoachCodeException(InvalidCoachCodeException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Coach Code")
                .message(e.getMessage())
                .build();
        log.warn("Invalid coach code: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ===== LIFTER EXCEPTIONS =====

    @ExceptionHandler(LifterNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLifterNotFoundException(LifterNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Lifter Not Found")
                .message(e.getMessage())
                .build();
        log.warn("Lifter not found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LifterAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleLifterAlreadyExistsException(LifterAlreadyExistsException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Lifter Already Exists")
                .message(e.getMessage())
                .build();
        log.warn("Lifter already exists: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // ===== CONNECTION REQUEST EXCEPTIONS =====

    @ExceptionHandler(ConnectionRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConnectionRequestNotFoundException(ConnectionRequestNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Connection Request Not Found")
                .message(e.getMessage())
                .build();
        log.warn("Connection request not found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateConnectionRequestException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateConnectionRequestException(DuplicateConnectionRequestException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Duplicate Connection Request")
                .message(e.getMessage())
                .build();
        log.warn("Duplicate connection request: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidConnectionRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidConnectionRequestException(InvalidConnectionRequestException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Connection Request")
                .message(e.getMessage())
                .build();
        log.warn("Invalid connection request: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ===== WORKOUT EXCEPTIONS =====

    @ExceptionHandler(WorkoutPlanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkoutPlanNotFoundException(WorkoutPlanNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Workout Plan Not Found")
                .message(e.getMessage())
                .build();
        log.warn("Workout plan not found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(WorkoutWeekNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkoutWeekNotFoundException(WorkoutWeekNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Workout Week Not Found")
                .message(e.getMessage())
                .build();
        log.warn("Workout week not found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(WorkoutDayNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkoutDayNotFoundException(WorkoutDayNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Workout Day Not Found")
                .message(e.getMessage())
                .build();
        log.warn("Workout day not found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExerciseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExerciseNotFoundException(ExerciseNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Exercise Not Found")
                .message(e.getMessage())
                .build();
        log.warn("Exercise not found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(WorkoutSetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkoutSetNotFoundException(WorkoutSetNotFoundException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Workout Set Not Found")
                .message(e.getMessage())
                .build();
        log.warn("Workout set not found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidWorkoutStructureException.class)
    public ResponseEntity<ErrorResponse> handleInvalidWorkoutStructureException(InvalidWorkoutStructureException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Workout Structure")
                .message(e.getMessage())
                .build();
        log.warn("Invalid workout structure: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WorkoutPlanAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleWorkoutPlanAlreadyAssignedException(WorkoutPlanAlreadyAssignedException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Workout Plan Already Assigned")
                .message(e.getMessage())
                .build();
        log.warn("Workout plan already assigned: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedWorkoutAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedWorkoutAccessException(UnauthorizedWorkoutAccessException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Unauthorized Workout Access")
                .message(e.getMessage())
                .build();
        log.warn("Unauthorized workout access: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(WorkoutPlanInactiveException.class)
    public ResponseEntity<ErrorResponse> handleWorkoutPlanInactiveException(WorkoutPlanInactiveException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Workout Plan Inactive")
                .message(e.getMessage())
                .build();
        log.warn("Workout plan inactive: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateWorkoutStructureException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateWorkoutStructureException(DuplicateWorkoutStructureException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Duplicate Workout Structure")
                .message(e.getMessage())
                .build();
        log.warn("Duplicate workout structure: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // ===== STANDARD EXCEPTIONS =====

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Request")
                .message(e.getMessage())
                .build();
        log.warn("Illegal argument: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String message = "Data integrity violation";

        // Extract more user-friendly message from the exception
        if (e.getMessage().contains("duplicate key")) {
            message = "A record with this information already exists";
        } else if (e.getMessage().contains("foreign key")) {
            message = "Cannot delete this record as it is referenced by other data";
        } else if (e.getMessage().contains("not null")) {
            message = "Required field is missing";
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message(message)
                .build();
        log.warn("Data integrity violation: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Request Body")
                .message("Request body is malformed or missing required fields")
                .build();
        log.warn("Invalid request body: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("Invalid value '%s' for parameter '%s'", e.getValue(), e.getName());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Parameter")
                .message(message)
                .build();
        log.warn("Method argument type mismatch: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = String.format("Required parameter '%s' is missing", e.getParameterName());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Missing Parameter")
                .message(message)
                .build();
        log.warn("Missing request parameter: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        String message = String.format("Required header '%s' is missing", e.getHeaderName());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Missing Header")
                .message(message)
                .build();
        log.warn("Missing request header: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String message = String.format("HTTP method '%s' is not supported for this endpoint", e.getMethod());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message(message)
                .build();
        log.warn("HTTP method not supported: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        String message = String.format("No handler found for %s %s", e.getHttpMethod(), e.getRequestURL());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Endpoint Not Found")
                .message(message)
                .build();
        log.warn("No handler found: {}", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ===== CATCH-ALL EXCEPTION HANDLER =====

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .build();
        log.error("Unexpected error: ", e);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}