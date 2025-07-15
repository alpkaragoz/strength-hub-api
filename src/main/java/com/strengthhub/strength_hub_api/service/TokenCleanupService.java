package com.strengthhub.strength_hub_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final AuthService authService;

    // Clean up expired and revoked refresh tokens every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupTokens() {
        log.info("Starting cleanup of expired and revoked refresh tokens");
        authService.cleanupExpiredTokens();
        log.info("Completed cleanup of expired and revoked refresh tokens");
    }

    // Also run cleanup every 6 hours for more frequent cleanup
    @Scheduled(fixedRate = 21600000) // 6 hours in milliseconds
    public void periodicCleanup() {
        log.debug("Running periodic token cleanup");
        authService.cleanupExpiredTokens();
    }
}