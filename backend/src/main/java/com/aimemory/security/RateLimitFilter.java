package com.aimemory.security;

import com.aimemory.entity.Tenant;
import com.aimemory.service.UsageService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final UsageService usageService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Tenant tenant;
        try {
            tenant = TenantContext.get();
        } catch (IllegalStateException e) {
            chain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = rateLimitService.tryConsume(tenant);

        if (probe.isConsumed()) {
            usageService.recordApiCallAsync(tenant);

            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
            return;
        }

        long waitSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000L);

        log.warn("🚫 Rate limit excedido: tenant={}, espera={}s", tenant.getId(), waitSeconds);

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(waitSeconds));
        response.setHeader("X-RateLimit-Remaining", "0");

        String body = String.format(
                "{\"error\":true,\"status\":429,\"message\":\"Rate limit excedido. Tente novamente em %d segundos.\",\"retryAfter\":%d,\"plan\":\"%s\",\"limitPerMinute\":%d,\"timestamp\":\"%s\"}",
                waitSeconds,
                waitSeconds,
                tenant.getPlan().name(),
                tenant.getRateLimitPerMinute(),
                LocalDateTime.now().toString()
        );

        response.getWriter().write(body);
    }
}