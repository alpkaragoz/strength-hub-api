package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.request.CoachRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.request.CoachUpdateRequest;
import com.strengthhub.strength_hub_api.dto.response.CoachResponse;
import com.strengthhub.strength_hub_api.dto.response.CoachDetailResponse;
import com.strengthhub.strength_hub_api.dto.response.CoachSummaryResponse;
import com.strengthhub.strength_hub_api.dto.response.LifterSummaryResponse;
import com.strengthhub.strength_hub_api.exception.coach.CoachAlreadyExistsException;
import com.strengthhub.strength_hub_api.exception.coach.CoachNotFoundException;
import com.strengthhub.strength_hub_api.exception.coach.InvalidCoachAssignmentException;
import com.strengthhub.strength_hub_api.exception.coach.InvalidCoachCodeException;
import com.strengthhub.strength_hub_api.exception.lifter.LifterNotFoundException;
import com.strengthhub.strength_hub_api.exception.user.UserNotFoundException;
import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.Lifter;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.CoachRepository;
import com.strengthhub.strength_hub_api.repository.LifterRepository;
import com.strengthhub.strength_hub_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoachService {

    private final CoachRepository coachRepository;
    private final UserRepository userRepository;
    private final LifterRepository lifterRepository;
    private final CoachCodeService coachCodeService;

    @Transactional
    public CoachResponse createCoach(UUID userId, CoachRegistrationRequest request) {
        log.info("Creating coach profile for user with id: {}", userId);

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));

        // Check if coach profile already exists for this user
        if (coachRepository.existsById(userId)) {
            throw new CoachAlreadyExistsException(userId);
        }

        // Process coach code if provided
        if (request.getCoachCode() != null && !request.getCoachCode().trim().isEmpty()) {
                if (coachCodeService.validateCoachCode(request.getCoachCode())) {
                    // Create coach profile
                    Coach coach = Coach.builder()
                            .coachId(userId)
                            .app_user(user)
                            .bio(request.getBio() != null ? request.getBio() : "")
                            .certifications(request.getCertifications() != null ? request.getCertifications() : "")
                            .build();

                    Coach savedCoach = coachRepository.save(coach);
                    log.info("Coach profile created for user with id: {}", userId);

                    return mapToResponse(savedCoach);
                } else {
                    log.warn("Invalid coach code provided during registration.");
                    // Continue with just lifter registration, don't fail the entire process
                }
        }

        // Create coach profile
        Coach coach = Coach.builder()
                .coachId(userId)
                .app_user(user)
                .bio(request.getBio() != null ? request.getBio() : "")
                .certifications(request.getCertifications() != null ? request.getCertifications() : "")
                .build();

        Coach savedCoach = coachRepository.save(coach);
        log.info("Coach profile created for user with id: {}", userId);

        return mapToResponse(savedCoach);
    }

    @Transactional(readOnly = true)
    public CoachDetailResponse getCoachById(UUID coachId) {
        log.info("Fetching coach with id: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new CoachNotFoundException(coachId));

        return mapToDetailResponse(coach);
    }

    @Transactional(readOnly = true)
    public List<CoachResponse> getAllCoaches() {
        log.info("Fetching all coaches");

        return coachRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CoachResponse updateCoach(UUID coachId, CoachUpdateRequest request) {
        log.info("Updating coach with id: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new CoachNotFoundException(coachId));

        if (request.getBio() != null) {
            coach.setBio(request.getBio());
        }

        if (request.getCertifications() != null) {
            coach.setCertifications(request.getCertifications());
        }

        Coach updatedCoach = coachRepository.save(coach);
        log.info("Coach updated with id: {}", coachId);

        return mapToResponse(updatedCoach);
    }

    @Transactional
    public void deleteCoach(UUID coachId) {
        log.info("Deleting coach with id: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new CoachNotFoundException(coachId));

        // Remove all lifter associations before deleting coach
        coach.getLifters().forEach(lifter -> lifter.setCoach(null));

        coachRepository.delete(coach);
        log.info("Coach deleted with id: {}", coachId);
    }

    @Transactional(readOnly = true)
    public List<LifterSummaryResponse> getCoachLifters(UUID coachId) {
        log.info("Fetching lifters for coach with id: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new CoachNotFoundException(coachId));

        return coach.getLifters()
                .stream()
                .map(this::mapToLifterSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignLifterToCoach(UUID coachId, UUID lifterId) {
        log.info("Assigning lifter {} to coach {}", lifterId, coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new CoachNotFoundException(coachId));

        Lifter lifter = lifterRepository.findById(lifterId)
                .orElseThrow(() -> new LifterNotFoundException(lifterId));

        // Remove lifter from current coach if they have one
        if (lifter.hasCoach()) {
            lifter.getCoach().removeLifter(lifter);
        }

        // Assign lifter to new coach
        coach.addLifter(lifter);

        coachRepository.save(coach);
        log.info("Lifter {} assigned to coach {}", lifterId, coachId);
    }

    @Transactional
    public void removeLifterFromCoach(UUID coachId, UUID lifterId) {
        log.info("Removing lifter {} from coach {}", lifterId, coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new CoachNotFoundException(coachId));

        Lifter lifter = lifterRepository.findById(lifterId)
                .orElseThrow(() -> new LifterNotFoundException(lifterId));

        if (!lifter.hasCoach() || !lifter.getCoach().getCoachId().equals(coachId)) {
            throw new InvalidCoachAssignmentException("Lifter is not assigned to this coach");
        }

        coach.removeLifter(lifter);
        coachRepository.save(coach);
        log.info("Lifter {} removed from coach {}", lifterId, coachId);
    }

    private CoachResponse mapToResponse(Coach coach) {
        User user = coach.getApp_user();
        return CoachResponse.builder()
                .coachId(coach.getCoachId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .bio(coach.getBio())
                .certifications(coach.getCertifications())
                .lifterCount(coach.getLifterCount())
                .build();
    }

    private CoachDetailResponse mapToDetailResponse(Coach coach) {
        User user = coach.getApp_user();
        List<LifterSummaryResponse> lifters = coach.getLifters()
                .stream()
                .map(this::mapToLifterSummaryResponse)
                .collect(Collectors.toList());

        return CoachDetailResponse.builder()
                .coachId(coach.getCoachId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .bio(coach.getBio())
                .certifications(coach.getCertifications())
                .lifters(lifters)
                .build();
    }

    private LifterSummaryResponse mapToLifterSummaryResponse(Lifter lifter) {
        User user = lifter.getApp_user();
        return LifterSummaryResponse.builder()
                .lifterId(lifter.getLifterId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .build();
    }

    public CoachSummaryResponse mapToSummaryResponse(Coach coach) {
        User user = coach.getApp_user();
        return CoachSummaryResponse.builder()
                .coachId(coach.getCoachId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .build();
    }
}
