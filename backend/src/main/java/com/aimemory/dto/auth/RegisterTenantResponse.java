package com.aimemory.dto.auth;

import java.util.UUID;

public record RegisterTenantResponse(
        UUID tenantId,
        String name,
        String email,
        String apiKey,        // ← mostrado UMA vez, nunca mais
        String environment,
        String plan
) {}