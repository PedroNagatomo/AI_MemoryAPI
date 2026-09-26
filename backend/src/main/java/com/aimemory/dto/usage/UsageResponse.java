package com.aimemory.dto.usage;

import com.aimemory.entity.Tenant;
import com.aimemory.entity.TenantUsage;

import java.time.LocalDateTime;

public record UsageResponse(
        String period,
        String plan,

        // Memórias
        long memoriesUsed,
        long memoriesQuota,
        double memoriesPercent,

        // Tokens
        long tokensUsed,
        long tokensQuota,
        double tokensPercent,

        // Contadores
        long extractionsCount,
        long searchesCount,
        long apiCallsCount,

        LocalDateTime resetAt
) {
    public static UsageResponse from(Tenant tenant, TenantUsage usage, LocalDateTime resetAt) {
        Tenant.PlanLimits limits = tenant.getPlanLimits();

        long memoriesUsed = usage != null ? usage.getMemoriesCount() : 0L;
        long tokensUsed = usage != null ? usage.getTokensUsed() : 0L;

        return new UsageResponse(
                usage != null ? usage.getPeriod() : null,
                tenant.getPlan().name(),
                memoriesUsed,
                limits.memories(),
                limits.memories() > 0 ? (double) memoriesUsed / limits.memories() : 0.0,
                tokensUsed,
                limits.tokens(),
                limits.tokens() > 0 ? (double) tokensUsed / limits.tokens() : 0.0,
                usage != null ? usage.getExtractionsCount() : 0L,
                usage != null ? usage.getSearchesCount() : 0L,
                usage != null ? usage.getApiCallsCount() : 0L,
                resetAt
        );
    }
}