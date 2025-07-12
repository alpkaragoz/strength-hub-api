package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.response.CoachSummaryResponse;
import com.strengthhub.strength_hub_api.dto.response.LifterResponse;
import com.strengthhub.strength_hub_api.exception.coach.CoachNotFoundException;
import com.strengthhub.strength_hub_api.exception.lifter.LifterNotFoundException;
import com.strengthhub.strength_hub_api.model.Coach;
import com.strengthhub.strength_hub_api.model.Lifter;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.CoachRepository;
import com.strengthhub.strength_hub_api.repository.LifterRepository;
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
public class LifterService {

    private final LifterRepository lifterRepository;
    private final CoachRepository coachRepository;
    private final CoachService coachService;

    @Transactional(readOnly = true)
    public LifterResponse getLifterById(UUID lifterId) {
        log.info("Fetching lifter with id: {}", lifterId);

        Lifter lifter = lifterRepository.findById(lifterId)
                .orElseThrow(() -> new LifterNotFoundException(lifterId));

        return mapToResponse(lifter);
    }

    @Transactional(readOnly = true)
    public List<LifterResponse> getAllLifters() {
        log.info("Fetching all lifters");

        return lifterRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteLifter(UUID lifterId) {
        log.info("Deleting lifter with id: {}", lifterId);

        Lifter lifter = lifterRepository.findById(lifterId)
                .orElseThrow(() -> new LifterNotFoundException(lifterId));

        // Remove lifter from coach's list if they have a coach
        if (lifter.hasCoach()) {
            lifter.getCoach().removeLifter(lifter);
        }

        lifterRepository.delete(lifter);
        log.info("Lifter deleted with id: {}", lifterId);
    }

    @Transactional
    public LifterResponse assignCoachToLifter(UUID lifterId, UUID coachId) {
        log.info("Assigning coach {} to lifter {}", coachId, lifterId);

        Lifter lifter = lifterRepository.findById(lifterId)
                .orElseThrow(() -> new LifterNotFoundException(lifterId));

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new CoachNotFoundException(coachId));

        // Remove lifter from current coach if they have one
        if (lifter.hasCoach()) {
            lifter.getCoach().removeLifter(lifter);
        }

        // Assign new coach to lifter
        coach.addLifter(lifter);

        lifterRepository.save(lifter);
        log.info("Coach {} assigned to lifter {}", coachId, lifterId);

        return mapToResponse(lifter);
    }

    @Transactional
    public LifterResponse removeCoachFromLifter(UUID lifterId) {
        log.info("Removing coach from lifter {}", lifterId);

        Lifter lifter = lifterRepository.findById(lifterId)
                .orElseThrow(() -> new LifterNotFoundException(lifterId));

        if (lifter.hasCoach()) {
            lifter.getCoach().removeLifter(lifter);
            lifterRepository.save(lifter);
            log.info("Coach removed from lifter {}", lifterId);
        } else {
            log.info("Lifter {} has no coach to remove", lifterId);
        }

        return mapToResponse(lifter);
    }

    @Transactional(readOnly = true)
    public List<LifterResponse> getLiftersWithoutCoach() {
        log.info("Fetching lifters without coach");

        return lifterRepository.findAll()
                .stream()
                .filter(lifter -> !lifter.hasCoach())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LifterResponse> getLiftersByCoach(UUID coachId) {
        log.info("Fetching lifters for coach with id: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new CoachNotFoundException(coachId));

        return coach.getLifters()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LifterResponse mapToResponse(Lifter lifter) {
        User user = lifter.getApp_user();
        CoachSummaryResponse coachSummary = null;

        if (lifter.hasCoach()) {
            coachSummary = coachService.mapToSummaryResponse(lifter.getCoach());
        }

        return LifterResponse.builder()
                .lifterId(lifter.getLifterId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .coach(coachSummary)
                .build();
    }
}

