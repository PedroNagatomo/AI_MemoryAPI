package com.aimemory.exception;

import lombok.Getter;

@Getter
public class QuotaExceededException extends RuntimeException {

    private final String quotaType;   // "memories" | "tokens"
    private final long current;
    private final long limit;
    private final String plan;
    private final String resetAt;

    public QuotaExceededException(String quotaType, long current, long limit, String plan, String resetAt) {
        super(String.format(
                "Quota de %s excedida: %d/%d (plano %s, reseta em %s)",
                quotaType, current, limit, plan, resetAt
        ));
        this.quotaType = quotaType;
        this.current = current;
        this.limit = limit;
        this.plan = plan;
        this.resetAt = resetAt;
    }
}