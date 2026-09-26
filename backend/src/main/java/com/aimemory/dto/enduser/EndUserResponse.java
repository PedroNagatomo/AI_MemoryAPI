package com.aimemory.dto.enduser;

import com.aimemory.entity.EndUser;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record EndUserResponse(
        UUID id,
        String externalId,
        Map<String, Object> metadata,
        LocalDateTime createdAt
) {
    public static EndUserResponse from(EndUser u) {
        return new EndUserResponse(u.getId(), u.getExternalId(), u.getMetadata(), u.getCreatedAt());
    }
}