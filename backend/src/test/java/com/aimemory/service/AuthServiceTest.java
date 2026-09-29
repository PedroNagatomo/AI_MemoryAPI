package com.aimemory.service;

import com.aimemory.AbstractIntegrationTest;
import com.aimemory.dto.auth.RegisterTenantRequest;
import com.aimemory.dto.auth.RegisterTenantResponse;
import com.aimemory.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

class AuthServiceTest extends AbstractIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private TenantRepository tenantRepository;

    @BeforeEach
    void cleanup() {
        tenantRepository.deleteAll();
    }

    @Test
    void shouldRegisterNewTenant() {
        var req = new RegisterTenantRequest("Meu App", "new@example.com");

        RegisterTenantResponse resp = authService.register(req);

        assertThat(resp.tenantId()).isNotNull();
        assertThat(resp.email()).isEqualTo("new@example.com");
        assertThat(resp.apiKey()).startsWith("amk_test_");
        assertThat(resp.plan()).isEqualTo("FREE");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        authService.register(new RegisterTenantRequest("A", "dup@example.com"));

        assertThatThrownBy(() ->
                authService.register(new RegisterTenantRequest("B", "dup@example.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já cadastrado");
    }

    @Test
    void shouldStoreHashedApiKey() {
        var resp = authService.register(new RegisterTenantRequest("App", "hash@example.com"));

        var tenant = tenantRepository.findById(resp.tenantId()).orElseThrow();

        assertThat(tenant.getApiKeyHash()).isNotEqualTo(resp.apiKey());  // nunca plain
        assertThat(tenant.getApiKeyHash()).hasSize(64);                  // SHA-256 hex
        assertThat(tenant.getApiKeyPrefix()).isEqualTo(resp.apiKey().substring(0, 12));
    }
}