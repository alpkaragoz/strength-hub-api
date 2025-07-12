package com.strengthhub.strength_hub_api.controller;

import com.strengthhub.strength_hub_api.dto.request.CoachCodeGenerationRequest;
import com.strengthhub.strength_hub_api.dto.response.CoachCodeResponse;
import com.strengthhub.strength_hub_api.service.CoachCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CoachCodeResponse> generateCoachCode(@Valid @RequestBody CoachCodeGenerationRequest request, @RequestBody String secretKey) {
        CoachCodeResponse generatedCode = coachCodeService.generateCoachCode(secretKey);
        return new ResponseEntity<>(generatedCode, HttpStatus.CREATED);
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateCoachCode(@RequestParam String code) {
        boolean isValid = coachCodeService.validateCoachCode(code);
        return ResponseEntity.ok(isValid);
    }
}
