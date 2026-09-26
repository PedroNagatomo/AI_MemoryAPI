package com.aimemory.ai.groq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.ai.groq")
@Getter
@Setter
public class GroqConfig {

    /** API key da Groq (via env var GROQ_API_KEY) */
    private String apiKey;

    /** Base URL da API */
    private String baseUrl = "https://api.groq.com/openai/v1";

    /** Modelo padrão para chat */
    private String model = "openai/gpt-oss-20b";

    /** Timeout em segundos para requisições */
    private int timeoutSeconds = 60;

    /** Max tokens da resposta */
    private int maxTokens = 2048;

    /** Temperatura (0.0 = determinístico, 1.0 = criativo) */
    private double temperature = 0.3;
}