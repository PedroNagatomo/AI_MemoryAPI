package com.aimemory.controller;

import com.aimemory.ai.extraction.ExtractionService;
import com.aimemory.ai.groq.GroqClient;
import com.aimemory.ai.model.AIResponse;
import com.aimemory.ai.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GroqClient groqClient;
    private final ExtractionService extractionService;


    /**
     * Endpoint só pra testar se a Groq está configurada e respondendo.
     * Em produção, remover ou proteger com role ADMIN.
     */
    @PostMapping("/ai-test")
    public ResponseEntity<?> testAI(@RequestBody(required = false) Map<String, String> body) {
        if (!groqClient.isConfigured()) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", true,
                    "message", "GROQ_API_KEY não configurada"
            ));
        }

        String prompt = body != null ? body.getOrDefault("prompt", "Diga OK") : "Diga OK";

        AIResponse response = groqClient.chat(
                "Você é um assistente que responde de forma curta e direta.",
                prompt
        );

        return ResponseEntity.ok(Map.of(
                "content", response.content(),
                "model", response.model(),
                "tokens", response.totalTokens() != null ? response.totalTokens() : 0
        ));
    }

    @PostMapping("/ai-extract-test")
    public ResponseEntity<?> testExtraction(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> rawMessages =
                (List<Map<String, String>>) body.getOrDefault("messages", List.of());

        if (rawMessages.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", true,
                    "message", "Envie 'messages' como array de {role, content}"
            ));
        }

        List<ChatMessage> messages = rawMessages.stream()
                .map(m -> new ChatMessage(m.get("role"), m.get("content")))
                .toList();

        var result = extractionService.extract(messages);

        return ResponseEntity.ok(Map.of(
                "extracted", result.memories().size(),
                "memories", result.memories(),
                "tokensUsed", result.tokensUsed() != null ? result.tokensUsed() : 0
        ));
    }
}