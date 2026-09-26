package com.aimemory.security;

import com.aimemory.entity.Tenant;
import com.aimemory.service.UsageService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limit in-memory por tenant.
 * Cada tenant tem um bucket que recarrega continuamente.
 *
 * Para múltiplas instâncias, trocar por Redis (bucket4j-redis).
 */
@Service
@Slf4j
public class RateLimitService {

    // tenant ID → Bucket
    private final Map<UUID, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Tenta consumir 1 token do bucket do tenant.
     * Retorna o resultado (sucesso ou quanto esperar).
     */
    public ConsumptionProbe tryConsume(Tenant tenant) {
        Bucket bucket = buckets.computeIfAbsent(
                tenant.getId(),
                id -> createBucket(tenant.getRateLimitPerMinute())
        );
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    /**
     * Cria um bucket com capacidade = rate limit/min, recarga contínua.
     */
    private Bucket createBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Atualiza o bucket se o plano mudou.
     * Útil quando o tenant faz upgrade.
     */
    public void resetBucket(Tenant tenant) {
        buckets.put(
                tenant.getId(),
                createBucket(tenant.getRateLimitPerMinute())
        );
        log.info("🔄 Bucket resetado: tenant={}, novo limite={}/min",
                tenant.getId(), tenant.getRateLimitPerMinute());
    }

    /**
     * Limpa buckets antigos (chamado por @Scheduled).
     * Evita memory leak em memória longa.
     */
    public void cleanup(int maxIdleMinutes) {
        // TODO: adicionar timestamp de último uso
        // Por enquanto, limpa se passar de X buckets
        if (buckets.size() > 10_000) {
            log.warn("⚠️ Muitos buckets em memória ({}), limpando metade", buckets.size());
            buckets.clear();
        }
    }
}