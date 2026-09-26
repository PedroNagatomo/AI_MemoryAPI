package com.aimemory.controller;

import com.aimemory.dto.memory.*;
import com.aimemory.service.MemoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.aimemory.service.IngestService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/memories")
@RequiredArgsConstructor
@Tag(name = "Memories", description = "Criação, listagem, ingestão e busca de memórias")
public class MemoryController {

    private final MemoryService memoryService;
    private final IngestService ingestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar memória manualmente",
            description = "Útil quando você já sabe o que quer armazenar (sem IA)")
    public MemoryResponse create(@Valid @RequestBody CreateMemoryRequest req) {
        return memoryService.create(req);
    }

    @GetMapping
    @Operation(summary = "Listar memórias de um end-user",
            description = "Lista paginada, ordenada da mais recente para a mais antiga")
    public List<MemoryResponse> list(
            @Parameter(description = "ID do end-user", required = true)
            @RequestParam UUID endUserId,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Offset para paginação", example = "0")
            @RequestParam(defaultValue = "0") int offset) {
        return memoryService.listByEndUser(endUserId, limit, offset);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar memória por ID")
    public MemoryResponse get(@PathVariable UUID id) {
        return memoryService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft-delete de memória",
            description = "Marca a memória como deletada. Não é removida fisicamente (auditoria).")
    @ApiResponse(responseCode = "204", description = "Memória deletada")
    public void delete(@PathVariable UUID id) {
        memoryService.delete(id);
    }

    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Ingerir conversa e extrair memórias automaticamente",
            description = """
            Envia uma conversa (array de mensagens) e a IA extrai FATOS
            DURADOUROS sobre o usuário (nome, preferências, relações, etc.).

            Cada memória extraída é persistida e retornada com seu ID.

            **Custo:** ~1000 tokens Groq por conversa (tier gratuito).
            """
    )
    @ApiResponse(responseCode = "201", description = "Memórias extraídas e persistidas")
    @ApiResponse(responseCode = "502", description = "Falha na Groq (rate limit, timeout, etc.)")
    public IngestResponse ingest(@Valid @RequestBody IngestRequest request) {
        return ingestService.ingest(request);
    }

    @PostMapping("/search")
    @Operation(
            summary = "Buscar memórias por texto (Full-Text Search)",
            description = """
            Busca textual com stemming em português. Acha "café" buscando "cafe",
            e "correr" buscando "corri" (variações).

            Ordenação: relevância textual > importance > recência.
            """
    )
    public SearchResponse search(@Valid @RequestBody SearchRequest req) {
        return memoryService.search(req);
    }
}