package com.aimemory.dto.memory;

import com.aimemory.entity.Memory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SearchResponse(
        String query,
        int count,
        List<Hit> memories
) {
    public record Hit(
            UUID id,
            UUID endUserId,
            String content,
            String category,
            String source,
            String sourceId,
            Short importance,
            Map<String, Object> metadata,
            LocalDateTime createdAt
    ) {
        public static Hit from(Memory m) {
            return new Hit(
                    m.getId(),
                    m.getEndUser().getId(),
                    m.getContent(),
                    m.getCategory(),
                    m.getSource(),
                    m.getSourceId(),
                    m.getImportance(),
                    m.getMetadata(),
                    m.getCreatedAt()
            );
        }
    }
}