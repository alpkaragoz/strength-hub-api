package com.strengthhub.strength_hub_api.controller;

import com.strengthhub.strength_hub_api.dto.request.CoachAssignmentRequest;
import com.strengthhub.strength_hub_api.dto.response.LifterResponse;
import com.strengthhub.strength_hub_api.service.LifterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lifters")
@RequiredArgsConstructor
@Validated
public class LifterController {

    private final LifterService lifterService;

    @GetMapping("/{lifterId}")
    public ResponseEntity<LifterResponse> getLifterById(@PathVariable UUID lifterId) {
        LifterResponse lifter = lifterService.getLifterById(lifterId);
        return ResponseEntity.ok(lifter);
    }

    @GetMapping
    public ResponseEntity<List<LifterResponse>> getAllLifters() {
        List<LifterResponse> lifters = lifterService.getAllLifters();
        return ResponseEntity.ok(lifters);
    }

    @DeleteMapping("/{lifterId}")
    public ResponseEntity<Void> deleteLifter(@PathVariable UUID lifterId) {
        lifterService.deleteLifter(lifterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lifterId}/assign-coach")
    public ResponseEntity<LifterResponse> assignCoachToLifter(@PathVariable UUID lifterId,
                                                              @Valid @RequestBody CoachAssignmentRequest request) {
        LifterResponse updatedLifter = lifterService.assignCoachToLifter(lifterId, request.getCoachId());
        return ResponseEntity.ok(updatedLifter);
    }

    @DeleteMapping("/{lifterId}/remove-coach")
    public ResponseEntity<LifterResponse> removeCoachFromLifter(@PathVariable UUID lifterId) {
        LifterResponse updatedLifter = lifterService.removeCoachFromLifter(lifterId);
        return ResponseEntity.ok(updatedLifter);
    }

    @GetMapping("/without-coach")
    public ResponseEntity<List<LifterResponse>> getLiftersWithoutCoach() {
        List<LifterResponse> lifters = lifterService.getLiftersWithoutCoach();
        return ResponseEntity.ok(lifters);
    }

    @GetMapping("/by-coach/{coachId}")
    public ResponseEntity<List<LifterResponse>> getLiftersByCoach(@PathVariable UUID coachId) {
        List<LifterResponse> lifters = lifterService.getLiftersByCoach(coachId);
        return ResponseEntity.ok(lifters);
    }
}