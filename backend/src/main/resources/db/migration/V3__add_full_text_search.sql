ALTER TABLE memories
    ADD COLUMN search_vector tsvector
        GENERATED ALWAYS AS (
            to_tsvector('portuguese', coalesce(content, ''))
            ) STORED;

CREATE INDEX idx_memories_search_vector ON memories USING GIN(search_vector);

CREATE INDEX idx_memories_tenant_user_active
    ON memories(tenant_id, end_user_id)
    WHERE deleted_at IS NULL;