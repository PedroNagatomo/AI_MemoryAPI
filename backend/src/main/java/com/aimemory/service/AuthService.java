package com.aimemory.service;

import com.aimemory.dto.auth.RegisterTenantRequest;
import com.aimemory.dto.auth.RegisterTenantResponse;
import com.aimemory.entity.Tenant;
import com.aimemory.repository.TenantRepository;
import com.aimemory.util.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final TenantRepository tenantRepository;

    @Transactional
    public RegisterTenantResponse register(RegisterTenantRequest request) {
        if (tenantRepository.findByEmailAndDeletedAtIsNull(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        String environment = "TEST"; // todo mundo começa em sandbox
        String apiKey = ApiKeyGenerator.generate(environment);
        String hash = ApiKeyGenerator.hash(apiKey);
        String prefix = ApiKeyGenerator.extractPrefix(apiKey);

        Tenant tenant = Tenant.builder()
                .name(request.name())
                .email(request.email())
                .apiKeyHash(hash)
                .apiKeyPrefix(prefix)
                .plan(Tenant.Plan.FREE)
                .monthlyQuota(1000)
                .build();

        tenantRepository.save(tenant);
        log.info("✅ Tenant criado: {} ({})", tenant.getName(), tenant.getId());

        return new RegisterTenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getEmail(),
                apiKey,           // ← único momento que mostramos
                environment,
                tenant.getPlan().name()
        );
    }
}