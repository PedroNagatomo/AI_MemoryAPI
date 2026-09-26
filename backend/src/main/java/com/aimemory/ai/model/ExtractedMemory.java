package com.aimemory.ai.model;

public record ExtractedMemory(
        String content,
        String category,
        Integer importance
) {}