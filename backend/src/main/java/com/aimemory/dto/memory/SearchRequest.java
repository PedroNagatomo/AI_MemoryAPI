package com.aimemory.dto.memory;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record SearchRequest(
        @NotNull UUID endUserId,

        @NotBlank @Size(min = 2, max = 500)
        String query,

        @Min(1) @Max(50)
        Integer limit,

        List<String> categories
) {
    public int effectiveLimit() {
        return limit != null ? limit : 10;
    }
}