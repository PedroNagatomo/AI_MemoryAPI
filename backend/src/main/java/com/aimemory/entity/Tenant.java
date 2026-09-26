package com.aimemory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "api_key_hash", nullable = false)
    private String apiKeyHash;

    @Column(name = "api_key_prefix", nullable = false, length = 12)
    private String apiKeyPrefix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Plan plan = Plan.FREE;

    @Column(name = "monthly_quota", nullable = false)
    @Builder.Default
    private Integer monthlyQuota = 1000;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum Plan {
        FREE, PRO, BUSINESS
    }

    @Column(name = "memory_quota", nullable = false)
    @Builder.Default
    private Long memoryQuota = 1000L;

    @Column(name = "token_quota", nullable = false)
    @Builder.Default
    private Long tokenQuota = 50_000L;

    @Column(name = "rate_limit_per_minute", nullable = false)
    @Builder.Default
    private Integer rateLimitPerMinute = 100;

    public PlanLimits getPlanLimits() {
        return switch (plan) {
            case FREE -> new PlanLimits(1_000L, 50_000L, 100);
            case PRO -> new PlanLimits(50_000L, 1_000_000L, 500);
            case BUSINESS -> new PlanLimits(500_000L, 10_000_000L, 2_000);
        };
    }

    public record PlanLimits(long memories, long tokens, int rateLimitPerMinute) {}
}