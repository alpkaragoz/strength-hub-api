package com.strengthhub.strength_hub_api.controller;

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

    // TODO will change auth system no secretkey in body
    @PostMapping("/generate")
    public ResponseEntity<CoachCodeResponse> generateCoachCode(@Valid @RequestBody String secretKey) {
        CoachCodeResponse generatedCode = coachCodeService.generateCoachCode(secretKey);
        return new ResponseEntity<>(generatedCode, HttpStatus.CREATED);
    }
}
