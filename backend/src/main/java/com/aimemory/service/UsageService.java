package com.aimemory.service;

import com.aimemory.entity.Tenant;
import com.aimemory.entity.TenantUsage;
import com.aimemory.repository.TenantUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageService {

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TenantUsageRepository usageRepository;

    /**
     * Retorna (ou cria) o registro de uso do mês atual para o tenant.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TenantUsage getOrCreateCurrent(Tenant tenant) {
        String period = currentPeriod();

        // Fast path: já existe
        var existing = usageRepository.findByTenantIdAndPeriod(tenant.getId(), period);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Slow path: tentar criar, se outro thread já criou, pegar o que existe
        try {
            TenantUsage usage = TenantUsage.builder()
                    .tenant(tenant)
                    .period(period)
                    .build();
            return usageRepository.saveAndFlush(usage);
        } catch (DataIntegrityViolationException e) {
            log.debug("⚠️ Race condition detectada, buscando registro existente");
            return usageRepository.findByTenantIdAndPeriod(tenant.getId(), period)
                    .orElseThrow(() -> new IllegalStateException(
                            "Não foi possível criar nem recuperar TenantUsage", e));
        }
    }

    /**
     * Incrementa contadores de forma atômica.
     * REQUIRES_NEW garante commit imediato mesmo se o chamador falhar depois.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordMemories(Tenant tenant, long count) {
        TenantUsage usage = getOrCreateCurrent(tenant);
        usage.incrementMemories(count);
        usageRepository.save(usage);
        log.debug("📊 +{} memórias (tenant {} → {})", count, tenant.getId(), usage.getMemoriesCount());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordTokens(Tenant tenant, long count) {
        TenantUsage usage = getOrCreateCurrent(tenant);
        usage.incrementTokens(count);
        usageRepository.save(usage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordExtraction(Tenant tenant) {
        TenantUsage usage = getOrCreateCurrent(tenant);
        usage.incrementExtractions();
        usageRepository.save(usage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSearch(Tenant tenant) {
        TenantUsage usage = getOrCreateCurrent(tenant);
        usage.incrementSearches();
        usageRepository.save(usage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordApiCall(Tenant tenant) {
        TenantUsage usage = getOrCreateCurrent(tenant);
        usage.incrementApiCalls();
        usageRepository.save(usage);
    }

    private TenantUsage createNew(Tenant tenant, String period) {
        TenantUsage usage = TenantUsage.builder()
                .tenant(tenant)
                .period(period)
                .build();
        log.info("📊 Uso criado: tenant={}, period={}", tenant.getId(), period);
        return usageRepository.save(usage);
    }

    @Async("usageExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordApiCallAsync(Tenant tenant) {
        try {
            TenantUsage usage = getOrCreateCurrent(tenant);
            usage.incrementApiCalls();
            usageRepository.saveAndFlush(usage);   // ← flush imediato
        } catch (Exception e) {
            log.warn("⚠️ Falha ao registrar api call: {}", e.getMessage());
        }
    }

    public TenantUsage getCurrent(Tenant tenant) {
        return usageRepository.findByTenantIdAndPeriod(tenant.getId(), currentPeriod())
                .orElse(null);
    }

    public String currentPeriod() {
        return LocalDate.now().format(PERIOD_FORMAT);
    }
}