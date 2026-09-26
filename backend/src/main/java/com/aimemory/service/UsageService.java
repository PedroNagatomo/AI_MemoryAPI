package com.aimemory.service;

import com.aimemory.entity.Tenant;
import com.aimemory.entity.TenantUsage;
import com.aimemory.repository.TenantUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Transactional
    public TenantUsage getOrCreateCurrent(Tenant tenant) {
        String period = currentPeriod();
        return usageRepository.findByTenantIdAndPeriod(tenant.getId(), period)
                .orElseGet(() -> createNew(tenant, period));
    }

    /**
     * Retorna o uso atual sem criar (útil pra leitura).
     */
    public TenantUsage getCurrent(Tenant tenant) {
        return usageRepository.findByTenantIdAndPeriod(tenant.getId(), currentPeriod())
                .orElse(null);
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
            usageRepository.save(usage);
        } catch (Exception e) {
            log.warn("⚠️ Falha ao registrar api call: {}", e.getMessage());
        }
    }

    public String currentPeriod() {
        return LocalDate.now().format(PERIOD_FORMAT);
    }
}