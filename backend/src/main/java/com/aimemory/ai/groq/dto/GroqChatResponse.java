package com.aimemory.ai.groq.dto;

import java.util.List;

public record GroqChatResponse(
        String id,
        String model,
        List<Choice> choices,
        GroqUsage usage
) {
    public record Choice(int index, GroqMessage message, String finishReason) {}
}