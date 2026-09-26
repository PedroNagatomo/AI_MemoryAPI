package com.aimemory.exception;

import com.aimemory.ai.extraction.ExtractionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(error(e.getMessage(), 400));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurity(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(e.getMessage(), 403));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Erro de validação");
        return ResponseEntity.badRequest().body(error(msg, 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
        log.error("❌ Erro não tratado", e);
        return ResponseEntity.status(500).body(error("Erro interno", 500));
    }

    private Map<String, Object> error(String message, int status) {
        return Map.of(
                "error", true,
                "message", message,
                "status", status,
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(ExtractionException.class)
    public ResponseEntity<Map<String, Object>> handleExtraction(ExtractionException e) {
        log.error("❌ Falha na extração de IA", e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                error("Falha ao processar com IA: " + e.getMessage(), 502)
        );
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<Map<String, Object>> handleQuota(QuotaExceededException e) {
        log.warn("🚫 Quota excedida: {}", e.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", true);
        body.put("status", 402);
        body.put("message", e.getMessage());
        body.put("quotaType", e.getQuotaType());
        body.put("current", e.getCurrent());
        body.put("limit", e.getLimit());
        body.put("plan", e.getPlan());
        body.put("resetAt", e.getResetAt());
        body.put("upgradeUrl", "https://aimemory.dev/pricing");   // placeholder
        body.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
    }
}