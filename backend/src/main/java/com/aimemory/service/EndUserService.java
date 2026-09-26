package com.aimemory.service;

import com.aimemory.dto.enduser.CreateEndUserRequest;
import com.aimemory.dto.enduser.EndUserResponse;
import com.aimemory.entity.EndUser;
import com.aimemory.entity.Tenant;
import com.aimemory.repository.EndUserRepository;
import com.aimemory.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EndUserService {

    private final EndUserRepository endUserRepository;

    @Transactional
    public EndUserResponse create(CreateEndUserRequest req) {
        Tenant tenant = TenantContext.get();

        // idempotência: se já existe, retorna
        return endUserRepository.findByTenantIdAndExternalId(tenant.getId(), req.externalId())
                .map(EndUserResponse::from)
                .orElseGet(() -> {
                    EndUser user = EndUser.builder()
                            .tenant(tenant)
                            .externalId(req.externalId())
                            .metadata(req.metadata() != null ? req.metadata() : new java.util.HashMap<>())
                            .build();
                    endUserRepository.save(user);
                    log.info("👤 EndUser criado: {} (tenant {})", user.getExternalId(), tenant.getId());
                    return EndUserResponse.from(user);
                });
    }

    public List<EndUserResponse> listAll() {
        Tenant tenant = TenantContext.get();
        // simplificação MVP: listar por tenant
        return endUserRepository.findAll().stream()
                .filter(u -> u.getTenant().getId().equals(tenant.getId()))
                .map(EndUserResponse::from)
                .toList();
    }

    public EndUser requireById(UUID id) {
        Tenant tenant = TenantContext.get();
        EndUser user = endUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("EndUser não encontrado"));
        if (!user.getTenant().getId().equals(tenant.getId())) {
            throw new SecurityException("Acesso negado");
        }
        return user;
    }
}