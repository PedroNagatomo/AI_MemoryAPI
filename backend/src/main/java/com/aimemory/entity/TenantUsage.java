package com.aimemory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_usage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "period"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 7)
    private String period;   // 'YYYY-MM'

    @Column(name = "memories_count", nullable = false)
    @Builder.Default
    private Long memoriesCount = 0L;

    @Column(name = "tokens_used", nullable = false)
    @Builder.Default
    private Long tokensUsed = 0L;

    @Column(name = "extractions_count", nullable = false)
    @Builder.Default
    private Long extractionsCount = 0L;

    @Column(name = "searches_count", nullable = false)
    @Builder.Default
    private Long searchesCount = 0L;

    @Column(name = "api_calls_count", nullable = false)
    @Builder.Default
    private Long apiCallsCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Incrementos utilitários
    public void incrementMemories(long count) {
        this.memoriesCount += count;
    }

    public void incrementTokens(long count) {
        this.tokensUsed += count;
    }

    public void incrementExtractions() {
        this.extractionsCount++;
    }

    public void incrementSearches() {
        this.searchesCount++;
    }

    public void incrementApiCalls() {
        this.apiCallsCount++;
    }
}