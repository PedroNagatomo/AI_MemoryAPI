package com.aimemory.service;

import com.aimemory.ai.extraction.ExtractionService;
import com.aimemory.ai.model.ExtractedMemory;
import com.aimemory.dto.memory.IngestRequest;
import com.aimemory.dto.memory.IngestResponse;
import com.aimemory.dto.memory.MemoryResponse;
import com.aimemory.entity.EndUser;
import com.aimemory.entity.Memory;
import com.aimemory.entity.Tenant;
import com.aimemory.repository.MemoryRepository;
import com.aimemory.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestService {

    private final ExtractionService extractionService;
    private final EndUserService endUserService;
    private final MemoryRepository memoryRepository;
    private final UsageService usageService;
    private final QuotaService quotaService;

    /**
     * Extrai memórias de uma conversa e persiste no banco.
     * Multi-tenant safe: usa TenantContext pra isolar.
     */
    @Transactional
    public IngestResponse ingest(IngestRequest request) {
        long start = System.currentTimeMillis();

        Tenant tenant = TenantContext.get();
        EndUser endUser = endUserService.requireById(request.endUserId());

        quotaService.checkIngestQuota(tenant);

        log.info("📥 Ingest iniciado: tenant={}, endUser={}, mensagens={}",
                tenant.getId(), endUser.getId(), request.messages().size());

        // 1. Extrai
        var result = extractionService.extract(request.toChatMessages());
        List<ExtractedMemory> extracted = result.memories();

        // 2. Persiste
        List<Memory> persisted = new ArrayList<>();
        if (!extracted.isEmpty()) {
            for (ExtractedMemory em : extracted) {
                try {
                    Memory memory = toEntity(em, tenant, endUser, request);
                    memoryRepository.save(memory);
                    persisted.add(memory);
                } catch (Exception e) {
                    log.warn("⚠️ Falha ao persistir memória: {}", e.getMessage());
                }
            }
        }

        // 3. Registra uso (FORA da transação do caller, com REQUIRES_NEW)
        usageService.recordExtraction(tenant);
        usageService.recordMemories(tenant, persisted.size());
        if (result.tokensUsed() != null) {
            usageService.recordTokens(tenant, result.tokensUsed());
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("✅ Ingest concluído: {}/{} persistidas em {}ms",
                persisted.size(), extracted.size(), elapsed);

        List<MemoryResponse> responses = persisted.stream()
                .map(MemoryResponse::from)
                .toList();

        return new IngestResponse(
                extracted.size(),
                persisted.size(),
                result.tokensUsed(),
                responses
        );
    }

    /**
     * Converte ExtractedMemory → Memory entity.
     */
    private Memory toEntity(ExtractedMemory em, Tenant tenant, EndUser endUser, IngestRequest request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("extracted_by", "groq/gpt-oss-20b");
        metadata.put("extraction_version", "1.0");
        metadata.put("message_count", request.messages().size());

        return Memory.builder()
                .tenant(tenant)
                .endUser(endUser)
                .content(em.content())
                .category(em.category())
                .source("conversation")
                .sourceId(request.sourceId())
                .importance(em.importance() != null ? em.importance().shortValue() : (short) 5)
                .metadata(metadata)
                .build();
    }
}