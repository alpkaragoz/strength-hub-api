package com.strengthhub.strength_hub_api.controller;

import com.strengthhub.strength_hub_api.dto.request.coach.CoachRegistrationRequest;
import com.strengthhub.strength_hub_api.dto.request.coach.CoachUpdateRequest;
import com.strengthhub.strength_hub_api.dto.response.coach.CoachResponse;
import com.strengthhub.strength_hub_api.dto.response.coach.CoachDetailResponse;
import com.strengthhub.strength_hub_api.dto.response.lifter.LifterSummaryResponse;
import com.strengthhub.strength_hub_api.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coach")
@RequiredArgsConstructor
@Validated
public class CoachController {

    private final CoachService coachService;

    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.userId")
    public ResponseEntity<CoachResponse> createCoach(@PathVariable UUID userId,
                                                     @Valid @RequestBody CoachRegistrationRequest request) {
        CoachResponse coach = coachService.createCoach(userId, request);
        return new ResponseEntity<>(coach, HttpStatus.CREATED);
    }

    @GetMapping("/{coachId}")
    public ResponseEntity<CoachDetailResponse> getCoachById(@PathVariable UUID coachId) {
        CoachDetailResponse coach = coachService.getCoachById(coachId);
        return ResponseEntity.ok(coach);
    }

    @GetMapping
    public ResponseEntity<List<CoachResponse>> getAllCoaches() {
        List<CoachResponse> coaches = coachService.getAllCoaches();
        return ResponseEntity.ok(coaches);
    }

    @PutMapping("/{coachId}")
    @PreAuthorize("hasRole('ADMIN') or #coachId == authentication.principal.userId")
    public ResponseEntity<CoachResponse> updateCoach(@PathVariable UUID coachId,
                                                     @Valid @RequestBody CoachUpdateRequest request) {
        CoachResponse updatedCoach = coachService.updateCoach(coachId, request);
        return ResponseEntity.ok(updatedCoach);
    }

    @DeleteMapping("/{coachId}")
    @PreAuthorize("hasRole('ADMIN') or #coachId == authentication.principal.userId")
    public ResponseEntity<Void> deleteCoach(@PathVariable UUID coachId) {
        coachService.deleteCoach(coachId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{coachId}/lifters")
    public ResponseEntity<List<LifterSummaryResponse>> getCoachLifters(@PathVariable UUID coachId) {
        List<LifterSummaryResponse> lifters = coachService.getCoachLifters(coachId);
        return ResponseEntity.ok(lifters);
    }

    @PostMapping("/{coachId}/lifters/{lifterId}")
    public ResponseEntity<Void> assignLifterToCoach(@PathVariable UUID coachId,
                                                    @PathVariable UUID lifterId) {
        coachService.assignLifterToCoach(coachId, lifterId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{coachId}/lifters/{lifterId}")
    public ResponseEntity<Void> removeLifterFromCoach(@PathVariable UUID coachId,
                                                      @PathVariable UUID lifterId) {
        coachService.removeLifterFromCoach(coachId, lifterId);
        return ResponseEntity.ok().build();
    }
}
