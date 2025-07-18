package com.strengthhub.strength_hub_api.controller;

import com.strengthhub.strength_hub_api.dto.response.coach.CoachCodeResponse;
import com.strengthhub.strength_hub_api.service.CoachCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/coach-codes")
@RequiredArgsConstructor
@Validated
public class CoachCodeController {

    private final CoachCodeService coachCodeService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoachCodeResponse> generateCoachCode() {
        CoachCodeResponse generatedCode = coachCodeService.generateCoachCode();
        return new ResponseEntity<>(generatedCode, HttpStatus.CREATED);
    }
}
