package com.aimemory.service;

import com.aimemory.entity.Tenant;
import com.aimemory.entity.TenantUsage;
import com.aimemory.exception.QuotaExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotaService {

    private final UsageService usageService;

    /**
     * Estimativa conservadora de tokens por ingest.
     * Baseado em observação real: ~1000 tokens por conversa de 3 mensagens.
     */
    private static final long ESTIMATED_TOKENS_PER_INGEST = 1_500L;

    /**
     * Verifica se o tenant pode fazer um ingest AGORA.
     * Lança QuotaExceededException se não puder.
     */
    public void checkIngestQuota(Tenant tenant) {
        TenantUsage usage = usageService.getOrCreateCurrent(tenant);
        Tenant.PlanLimits limits = tenant.getPlanLimits();

        // 1. Checa quota de memórias
        // Uma ingest pode gerar até ~10 memórias, então estimamos conservador
        long estimatedMemories = 10L;
        long projectedMemories = usage.getMemoriesCount() + estimatedMemories;
        if (projectedMemories > limits.memories()) {
            log.warn("🚫 Quota de memórias excedida: tenant={}, uso={}/{}, plano={}",
                    tenant.getId(), usage.getMemoriesCount(), limits.memories(), tenant.getPlan());
            throw new QuotaExceededException(
                    "memories",
                    usage.getMemoriesCount(),
                    limits.memories(),
                    tenant.getPlan().name(),
                    nextResetDate()
            );
        }

        // 2. Checa quota de tokens
        long projectedTokens = usage.getTokensUsed() + ESTIMATED_TOKENS_PER_INGEST;
        if (projectedTokens > limits.tokens()) {
            log.warn("🚫 Quota de tokens excedida: tenant={}, uso={}/{}, plano={}",
                    tenant.getId(), usage.getTokensUsed(), limits.tokens(), tenant.getPlan());
            throw new QuotaExceededException(
                    "tokens",
                    usage.getTokensUsed(),
                    limits.tokens(),
                    tenant.getPlan().name(),
                    nextResetDate()
            );
        }

        log.debug("✅ Quota OK: tenant={}, memórias={}/{}, tokens={}/{}",
                tenant.getId(),
                usage.getMemoriesCount(), limits.memories(),
                usage.getTokensUsed(), limits.tokens());
    }

    /**
     * Verifica se o tenant pode fazer uma busca.
     * Busca não consome tokens, só conta pra rate limit.
     */
    public void checkSearchQuota(Tenant tenant) {
        // Por enquanto sem limite de busca — só registra uso
        // Futuro: se plano for FREE, limitar buscas por mês
    }

    private String nextResetDate() {
        LocalDate next = LocalDate.now().withDayOfMonth(1).plusMonths(1);
        return next.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}