package com.aimemory.repository;

import com.aimemory.AbstractIntegrationTest;
import com.aimemory.entity.EndUser;
import com.aimemory.entity.Memory;
import com.aimemory.entity.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryRepositoryTest extends AbstractIntegrationTest {

    @Autowired private MemoryRepository memoryRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private EndUserRepository endUserRepository;

    private Tenant tenant;
    private EndUser endUser;

    @BeforeEach
    void setup() {
        memoryRepository.deleteAll();
        endUserRepository.deleteAll();
        tenantRepository.deleteAll();

        tenant = tenantRepository.save(Tenant.builder()
                .name("Test Tenant")
                .email("test-" + System.nanoTime() + "@example.com")
                .apiKeyHash("hash")
                .apiKeyPrefix("amk_test_abc")
                .plan(Tenant.Plan.FREE)
                .build());

        endUser = endUserRepository.save(EndUser.builder()
                .tenant(tenant)
                .externalId("user_001")
                .build());
    }

    @Test
    void shouldSaveAndFindMemory() {
        Memory memory = memoryRepository.save(Memory.builder()
                .tenant(tenant)
                .endUser(endUser)
                .content("User loves coffee")
                .category("PREFERENCE")
                .source("test")
                .importance((short) 7)
                .build());

        assertThat(memory.getId()).isNotNull();
        assertThat(memory.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindByTenantAndEndUser() {
        memoryRepository.save(Memory.builder()
                .tenant(tenant).endUser(endUser)
                .content("Memory 1").source("test").build());
        memoryRepository.save(Memory.builder()
                .tenant(tenant).endUser(endUser)
                .content("Memory 2").source("test").build());

        List<Memory> results = memoryRepository
                .findByTenantIdAndEndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        tenant.getId(), endUser.getId(), PageRequest.of(0, 10));

        assertThat(results).hasSize(2);
    }

    @Test
    void shouldSearchWithFullText() {
        memoryRepository.save(Memory.builder()
                .tenant(tenant).endUser(endUser)
                .content("Usuário gosta de café sem açúcar").source("test").build());
        memoryRepository.save(Memory.builder()
                .tenant(tenant).endUser(endUser)
                .content("Usuário mora em São Paulo").source("test").build());

        // Busca "cafe" (sem acento) — deve achar "café"
        List<Memory> results = memoryRepository.searchByText(
                tenant.getId(), endUser.getId(), "cafe", 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getContent()).contains("café");
    }

    @Test
    void searchShouldNotReturnDeletedMemories() {
        Memory memory = memoryRepository.save(Memory.builder()
                .tenant(tenant).endUser(endUser)
                .content("Café especial").source("test").build());

        memory.setDeletedAt(LocalDateTime.now());
        memoryRepository.save(memory);

        List<Memory> results = memoryRepository.searchByText(
                tenant.getId(), endUser.getId(), "cafe", 10);

        assertThat(results).isEmpty();
    }
}