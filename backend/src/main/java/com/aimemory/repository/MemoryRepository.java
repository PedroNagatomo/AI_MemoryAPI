package com.aimemory.repository;

import com.aimemory.entity.Memory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {

    List<Memory> findByTenantIdAndEndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            UUID tenantId, UUID endUserId, Pageable pageable);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);

    @Query(value = """
        SELECT * FROM memories
        WHERE tenant_id = :tenantId
          AND end_user_id = :endUserId
          AND deleted_at IS NULL
          AND embedding IS NOT NULL
        ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Memory> findSimilar(
            @Param("tenantId") UUID tenantId,
            @Param("endUserId") UUID endUserId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit);

    @Query(value = """
    SELECT * FROM memories
    WHERE tenant_id = :tenantId
      AND end_user_id = :endUserId
      AND deleted_at IS NULL
      AND search_vector @@ plainto_tsquery('portuguese', :query)
    ORDER BY
      ts_rank(search_vector, plainto_tsquery('portuguese', :query)) DESC,
      importance DESC,
      created_at DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Memory> searchByText(
            @Param("tenantId") UUID tenantId,
            @Param("endUserId") UUID endUserId,
            @Param("query") String query,
            @Param("limit") int limit);
}