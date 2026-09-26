package com.aimemory.ai.extraction;

import com.aimemory.ai.model.ExtractedMemory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parseia o output do LLM e retorna lista de ExtractedMemory.
 * Lida com JSON imperfeito: markdown, texto extra, aspas, etc.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractionParser {

    private final ObjectMapper objectMapper;

    private static final Pattern JSON_ARRAY_PATTERN =
            Pattern.compile("\\[.*]", Pattern.DOTALL);
    private static final Pattern CODE_FENCE_PATTERN =
            Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL);

    public List<ExtractedMemory> parse(String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) {
            log.debug("Output vazio do LLM — retornando lista vazia");
            return List.of();
        }

        String cleaned = clean(rawOutput);
        log.debug("🔍 Parseando output ({} chars)", cleaned.length());

        try {
            List<ExtractedMemory> memories = objectMapper.readValue(
                    cleaned,
                    new TypeReference<List<ExtractedMemory>>() {}
            );

            // Validação
            memories = memories.stream()
                    .filter(m -> m.content() != null && !m.content().isBlank())
                    .map(this::normalize)
                    .toList();

            log.info("📦 Extraídas {} memórias", memories.size());
            return memories;

        } catch (Exception e) {
            log.error("❌ Falha ao parsear output do LLM. Raw (500 chars): {}",
                    rawOutput.substring(0, Math.min(500, rawOutput.length())));
            throw new ExtractionException("Falha ao parsear JSON do LLM: " + e.getMessage(), e);
        }
    }

    /**
     * Limpa o output: remove markdown fences, extrai array JSON.
     */
    private String clean(String raw) {
        String s = raw.trim();

        // 1. Remove code fences ```json ... ```
        Matcher fenceMatcher = CODE_FENCE_PATTERN.matcher(s);
        if (fenceMatcher.find()) {
            s = fenceMatcher.group(1).trim();
        }

        // 2. Se não começa com [, tenta extrair o array JSON
        if (!s.startsWith("[")) {
            Matcher arrayMatcher = JSON_ARRAY_PATTERN.matcher(s);
            if (arrayMatcher.find()) {
                s = arrayMatcher.group();
            }
        }

        return s;
    }

    /**
     * Normaliza campos: category uppercase, importance no range 1-10.
     */
    private ExtractedMemory normalize(ExtractedMemory m) {
        String category = m.category() != null
                ? m.category().toUpperCase().trim()
                : "FACT";

        // Valida categoria
        List<String> validCategories = List.of(
                "FACT", "PREFERENCE", "EVENT", "RELATIONSHIP", "GOAL", "CONTEXT"
        );
        if (!validCategories.contains(category)) {
            category = "FACT";
        }

        Integer importance = m.importance();
        if (importance == null) importance = 5;
        if (importance < 1) importance = 1;
        if (importance > 10) importance = 10;

        return new ExtractedMemory(
                m.content().trim(),
                category,
                importance
        );
    }
}