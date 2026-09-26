package com.aimemory.ai.groq;

import lombok.Getter;

@Getter
public class GroqException extends RuntimeException {

    private final boolean retryable;
    private final Integer statusCode;

    public GroqException(String message, Integer statusCode, boolean retryable) {
        super(message);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    public GroqException(String message, Throwable cause) {
        super(message, cause);
        this.retryable = false;
        this.statusCode = null;
    }

    public static GroqException rateLimit() {
        return new GroqException("Groq rate limit atingido", 429, true);
    }

    public static GroqException serverError(int status) {
        return new GroqException("Erro do servidor Groq: " + status, status, true);
    }

    public static GroqException clientError(int status, String body) {
        return new GroqException("Erro do cliente Groq: " + status + " - " + body, status, false);
    }
}