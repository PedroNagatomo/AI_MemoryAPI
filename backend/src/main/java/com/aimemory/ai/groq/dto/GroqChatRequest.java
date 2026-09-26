package com.aimemory.ai.groq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GroqChatRequest(
        String model,
        List<GroqMessage> messages,
        Double temperature,
        @JsonProperty("max_tokens") Integer maxTokens
) {}