package com.aimemory.dto.memory;

import java.util.List;

public record IngestResponse(
        int extracted,           // quantas a IA extraiu
        int persisted,           // quantas foram salvas com sucesso
        Integer tokensUsed,      // tokens consumidos
        List<MemoryResponse> memories
) {}