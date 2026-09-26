package com.aimemory.ai.groq;

import com.aimemory.ai.groq.dto.*;
import com.aimemory.ai.model.AIResponse;
import com.aimemory.ai.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
public class GroqClient {

    private final GroqConfig config;
    private final RestClient client;

    public GroqClient(GroqConfig config, RestClient.Builder builder) {
        this.config = config;
        this.client = builder
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            log.warn("⚠️ GROQ_API_KEY não configurada — chamadas de IA vão falhar");
        }
    }

    public AIResponse chat(List<ChatMessage> messages) {
        long start = System.currentTimeMillis();

        GroqChatRequest request = new GroqChatRequest(
                config.getModel(),
                messages.stream()
                        .map(m -> new GroqMessage(m.role(), m.content()))
                        .toList(),
                config.getTemperature(),
                config.getMaxTokens()
        );

        log.debug("🤖 Groq request: model={}, messages={}", config.getModel(), messages.size());

        try {
            GroqChatResponse response = client.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes());
                        log.error("❌ Groq 4xx: status={}, body={}", res.getStatusCode(), body);
                        throw GroqException.clientError(res.getStatusCode().value(), body);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("❌ Groq 5xx: status={}", res.getStatusCode());
                        throw GroqException.serverError(res.getStatusCode().value());
                    })
                    .body(GroqChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new GroqException("Resposta vazia da Groq", null, true);
            }

            String content = response.choices().get(0).message().content();
            Integer promptTokens = response.usage() != null ? response.usage().promptTokens() : null;
            Integer completionTokens = response.usage() != null ? response.usage().completionTokens() : null;
            Integer totalTokens = response.usage() != null ? response.usage().totalTokens() : null;

            long elapsed = System.currentTimeMillis() - start;
            log.info("✅ Groq respondeu em {}ms (tokens: {})",
                    elapsed, totalTokens != null ? totalTokens : "?");

            return new AIResponse(content, response.model(), promptTokens, completionTokens, totalTokens);

        } catch (GroqException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Erro inesperado ao chamar Groq", e);
            throw new GroqException("Erro ao chamar Groq: " + e.getMessage(), e);
        }
    }

    /** Atalho: system + user simples */
    public AIResponse chat(String systemPrompt, String userMessage) {
        return chat(List.of(
                ChatMessage.system(systemPrompt),
                ChatMessage.user(userMessage)
        ));
    }

    /** Verifica se a API está configurada */
    public boolean isConfigured() {
        return config.getApiKey() != null && !config.getApiKey().isBlank();
    }
}