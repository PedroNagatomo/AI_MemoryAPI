package com.aimemory.ai.groq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroqUsage(
        @JsonProperty("prompt_tokens") Integer promptTokens,
        @JsonProperty("completion_tokens") Integer completionTokens,
        @JsonProperty("total_tokens") Integer totalTokens
) {}