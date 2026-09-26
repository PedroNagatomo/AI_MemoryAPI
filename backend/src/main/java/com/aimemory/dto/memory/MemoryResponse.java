package com.aimemory.dto.memory;

import com.aimemory.entity.Memory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record MemoryResponse(
        UUID id,
        UUID endUserId,
        String content,
        String category,
        String source,
        String sourceId,
        Short importance,
        Map<String, Object> metadata,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MemoryResponse from(Memory m) {
        return new MemoryResponse(
                m.getId(),
                m.getEndUser().getId(),
                m.getContent(),
                m.getCategory(),
                m.getSource(),
                m.getSourceId(),
                m.getImportance(),
                m.getMetadata(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}