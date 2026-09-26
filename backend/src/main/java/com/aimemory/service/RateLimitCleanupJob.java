package com.aimemory.service;

import com.aimemory.security.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitCleanupJob {

    private final RateLimitService rateLimitService;

    /**
     * Limpa buckets inativos a cada 10 minutos.
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanup() {
        rateLimitService.cleanup(10);
    }
}