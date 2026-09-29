package com.aimemory.service;

import com.aimemory.AbstractIntegrationTest;
import com.aimemory.ai.extraction.ExtractionService;
import com.aimemory.ai.model.ExtractedMemory;
import com.aimemory.dto.memory.IngestRequest;
import com.aimemory.entity.EndUser;
import com.aimemory.entity.Tenant;
import com.aimemory.repository.EndUserRepository;
import com.aimemory.repository.MemoryRepository;
import com.aimemory.repository.TenantRepository;
import com.aimemory.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class IngestServiceTest extends AbstractIntegrationTest {

    @Autowired private IngestService ingestService;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private EndUserRepository endUserRepository;
    @Autowired private MemoryRepository memoryRepository;

    @MockBean private ExtractionService extractionService;   // ← mock!

    private Tenant tenant;
    private EndUser endUser;

    @BeforeEach
    void setup() {
        memoryRepository.deleteAll();
        endUserRepository.deleteAll();
        tenantRepository.deleteAll();

        tenant = tenantRepository.save(Tenant.builder()
                .name("Test")
                .email("ingest-" + System.nanoTime() + "@example.com")
                .apiKeyHash("hash")
                .apiKeyPrefix("amk_test_ing")
                .plan(Tenant.Plan.FREE)
                .build());

        endUser = endUserRepository.save(EndUser.builder()
                .tenant(tenant).externalId("user_001").build());

        TenantContext.set(tenant);
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void shouldExtractAndPersistMemories() {
        // Mock da extração
        var extractionResult = new ExtractionService.ExtractionResult(
                List.of(
                        new ExtractedMemory("User is named Pedro", "FACT", 10),
                        new ExtractedMemory("User loves coffee", "PREFERENCE", 7)
                ),
                1234
        );
        when(extractionService.extract(any())).thenReturn(extractionResult);

        var request = new IngestRequest(
                endUser.getId(),
                List.of(
                        new IngestRequest.Message("user", "Oi sou Pedro"),
                        new IngestRequest.Message("assistant", "Prazer!")
                ),
                "conv_001"
        );

        var response = ingestService.ingest(request);

        assertThat(response.extracted()).isEqualTo(2);
        assertThat(response.persisted()).isEqualTo(2);
        assertThat(response.tokensUsed()).isEqualTo(1234);
        assertThat(memoryRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyForNoExtraction() {
        when(extractionService.extract(any()))
                .thenReturn(new ExtractionService.ExtractionResult(List.of(), 500));

        var request = new IngestRequest(
                endUser.getId(),
                List.of(new IngestRequest.Message("user", "Oi")),
                null
        );

        var response = ingestService.ingest(request);

        assertThat(response.extracted()).isZero();
        assertThat(response.persisted()).isZero();
        assertThat(memoryRepository.count()).isZero();
    }
}