package com.aimemory.repository;

import com.aimemory.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByEmailAndDeletedAtIsNull(String email);
    Optional<Tenant> findByApiKeyPrefixAndDeletedAtIsNull(String prefix);
}