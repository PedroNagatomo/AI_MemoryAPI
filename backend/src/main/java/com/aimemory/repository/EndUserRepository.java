package com.aimemory.repository;

import com.aimemory.entity.EndUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EndUserRepository extends JpaRepository<EndUser, UUID> {
    Optional<EndUser> findByTenantIdAndExternalId(UUID tenantId, String externalId);
}