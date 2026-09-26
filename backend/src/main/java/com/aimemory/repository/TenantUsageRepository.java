package com.aimemory.repository;

import com.aimemory.entity.TenantUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantUsageRepository extends JpaRepository<TenantUsage, UUID> {

    Optional<TenantUsage> findByTenantIdAndPeriod(UUID tenantId, String period);
}