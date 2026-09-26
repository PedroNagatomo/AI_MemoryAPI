package com.aimemory.dto.memory;

import jakarta.validation.constraints.*;

import java.util.Map;
import java.util.UUID;

public record CreateMemoryRequest(
        @NotNull UUID endUserId,
        @NotBlank @Size(max = 10000) String content,
        @Size(max = 50) String category,
        @NotBlank @Size(max = 50) String source,
        @Size(max = 255) String sourceId,
        @Min(1) @Max(10) Integer importance,
        Map<String, Object> metadata
) {}