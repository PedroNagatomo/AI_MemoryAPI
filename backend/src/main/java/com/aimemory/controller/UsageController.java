package com.aimemory.controller;

import com.aimemory.dto.usage.UsageResponse;
import com.aimemory.entity.Tenant;
import com.aimemory.entity.TenantUsage;
import com.aimemory.security.TenantContext;
import com.aimemory.service.UsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/usage")
@RequiredArgsConstructor
@Tag(name = "Usage", description = "Uso do tenant e quotas do plano")
public class UsageController {

    private final UsageService usageService;

    @GetMapping
    @Operation(
            summary = "Consultar uso atual",
            description = "Retorna uso do mês, quotas do plano e data de reset"
    )
    public UsageResponse get() {
        Tenant tenant = TenantContext.get();
        TenantUsage usage = usageService.getCurrent(tenant);

        LocalDateTime resetAt = LocalDate.now()
                .withDayOfMonth(1)
                .plusMonths(1)
                .atStartOfDay();

        return UsageResponse.from(tenant, usage, resetAt);
    }
}