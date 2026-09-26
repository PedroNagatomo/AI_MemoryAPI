package com.aimemory.dto.enduser;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record CreateEndUserRequest(
        @NotBlank String externalId,
        Map<String, Object> metadata
) {}