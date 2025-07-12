package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.response.CoachCodeResponse;
import com.strengthhub.strength_hub_api.enums.CoachCodeStatus;
import com.strengthhub.strength_hub_api.exception.common.UnauthorizedAccessException;
import com.strengthhub.strength_hub_api.model.CoachCode;
import com.strengthhub.strength_hub_api.exception.coach.InvalidCoachCodeException;
import com.strengthhub.strength_hub_api.repository.CoachCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoachCodeService {

    private final CoachCodeRepository coachCodeRepository;

    @Transactional
    public CoachCodeResponse generateCoachCode(String secretKey) {
        // TODO
        if(!secretKey.equals("secret")) {
            throw new UnauthorizedAccessException("Cannot generate coach code, wrong credentials.");
        }
        log.info("Generating coach code");

        String code = generateUniqueCode();

        CoachCode coachCode = CoachCode.builder()
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24)) // 24-hour expiry
                .isUsed(false)
                .build();

        CoachCode savedCode = coachCodeRepository.save(coachCode);
        log.info("Coach code generated: {}", code);

        return mapToResponse(savedCode);
    }

    @Transactional(readOnly = true)
    public boolean validateCoachCode(String code) {
        Optional<CoachCode> codeEntity = coachCodeRepository.findByCode(code);

        if (codeEntity.isEmpty()) {
            return false; // Code doesn't exist
        }

        CoachCode coachCode = codeEntity.get();

        // Check if already used
        if (coachCode.getIsUsed()) {
            return false; // Already used (one-time only)
        }

        // Check if expired (24 hours)
        if (LocalDateTime.now().isAfter(coachCode.getExpiresAt())) {
            return false; // Expired
        }

        return true; // Valid code!
    }

    @Transactional
    public void useCoachCode(String code, UUID userId) {
        CoachCode coachCode = coachCodeRepository.findByCode(code)
                .orElseThrow(() -> new InvalidCoachCodeException("Code not found"));

        // Double-check validation before using
        if (coachCode.getIsUsed()) {
            throw new InvalidCoachCodeException("Code has already been used");
        }

        if (LocalDateTime.now().isAfter(coachCode.getExpiresAt())) {
            throw new InvalidCoachCodeException("Code has expired");
        }

        // Mark as used
        coachCode.setIsUsed(true);
        coachCode.setUsedBy(userId);
        coachCode.setUsedAt(LocalDateTime.now());

        coachCodeRepository.save(coachCode);
        log.info("Coach code {} used by user: {}", code, userId);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
        } while (coachCodeRepository.existsByCode(code));

        return code;
    }

    private CoachCodeResponse mapToResponse(CoachCode coachCode) {
        CoachCodeStatus status;
        if (coachCode.getIsUsed()) {
            status = CoachCodeStatus.USED;
        } else if (LocalDateTime.now().isAfter(coachCode.getExpiresAt())) {
            status = CoachCodeStatus.EXPIRED;
        } else {
            status = CoachCodeStatus.ACTIVE;
        }

        return CoachCodeResponse.builder()
                .codeId(coachCode.getCodeId())
                .code(coachCode.getCode())
                .expiresAt(coachCode.getExpiresAt())
                .isUsed(coachCode.getIsUsed())
                .usedBy(coachCode.getUsedBy())
                .createdAt(coachCode.getCreatedAt())
                .usedAt(coachCode.getUsedAt())
                .status(status)
                .build();
    }
}
