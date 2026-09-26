package com.aimemory.service;

import com.aimemory.dto.memory.CreateMemoryRequest;
import com.aimemory.dto.memory.MemoryResponse;
import com.aimemory.dto.memory.SearchRequest;
import com.aimemory.dto.memory.SearchResponse;
import com.aimemory.entity.EndUser;
import com.aimemory.entity.Memory;
import com.aimemory.entity.Tenant;
import com.aimemory.repository.MemoryRepository;
import com.aimemory.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryService {

    private final MemoryRepository memoryRepository;
    private final EndUserService endUserService;

    @Transactional
    public MemoryResponse create(CreateMemoryRequest req) {
        Tenant tenant = TenantContext.get();
        EndUser endUser = endUserService.requireById(req.endUserId());

        Memory memory = Memory.builder()
                .tenant(tenant)
                .endUser(endUser)
                .content(req.content())
                .category(req.category())
                .source(req.source())
                .sourceId(req.sourceId())
                .importance(req.importance() != null ? req.importance().shortValue() : (short) 5)
                .metadata(req.metadata() != null ? req.metadata() : new java.util.HashMap<>())
                .build();

        memoryRepository.save(memory);
        log.info("🧠 Memória criada: {} (tenant {})", memory.getId(), tenant.getId());
        return MemoryResponse.from(memory);
    }

    public List<MemoryResponse> listByEndUser(UUID endUserId, int limit, int offset) {
        Tenant tenant = TenantContext.get();
        endUserService.requireById(endUserId); // valida ownership

        return memoryRepository
                .findByTenantIdAndEndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                        tenant.getId(), endUserId, PageRequest.of(offset / limit, limit))
                .stream()
                .map(MemoryResponse::from)
                .toList();
    }

    public MemoryResponse getById(UUID id) {
        return MemoryResponse.from(requireById(id));
    }

    @Transactional
    public void delete(UUID id) {
        Memory memory = requireById(id);
        memory.setDeletedAt(LocalDateTime.now());
        memoryRepository.save(memory);
        log.info("🗑️ Memória soft-deleted: {}", id);
    }

    private Memory requireById(UUID id) {
        Tenant tenant = TenantContext.get();
        Memory memory = memoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Memória não encontrada"));
        if (!memory.getTenant().getId().equals(tenant.getId()) || memory.getDeletedAt() != null) {
            throw new SecurityException("Acesso negado");
        }
        return memory;
    }

    public SearchResponse search(SearchRequest req) {
        Tenant tenant = TenantContext.get();
        endUserService.requireById(req.endUserId()); // valida ownership

        log.debug("🔍 Busca: tenant={}, endUser={}, query='{}'",
                tenant.getId(), req.endUserId(), req.query());

        long start = System.currentTimeMillis();

        List<Memory> results = memoryRepository.searchByText(
                tenant.getId(),
                req.endUserId(),
                req.query(),
                req.effectiveLimit()
        );

        // Filtro opcional de categorias em memória (se vier no request)
        if (req.categories() != null && !req.categories().isEmpty()) {
            Set<String> allowed = req.categories().stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());
            results = results.stream()
                    .filter(m -> m.getCategory() != null && allowed.contains(m.getCategory()))
                    .toList();
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("🔍 Busca retornou {} resultados em {}ms", results.size(), elapsed);

        List<SearchResponse.Hit> hits = results.stream()
                .map(SearchResponse.Hit::from)
                .toList();

        return new SearchResponse(req.query(), hits.size(), hits);
    }
}