-- Habilita extensão pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- Tenants (cada dev que usa a API)
CREATE TABLE tenants (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         api_key_hash VARCHAR(255) NOT NULL,  -- hash da API key (nunca plain)
                         api_key_prefix VARCHAR(12) NOT NULL, -- primeiros 8 chars pra identificar
                         plan VARCHAR(50) NOT NULL DEFAULT 'FREE',
                         monthly_quota INTEGER NOT NULL DEFAULT 1000,
                         created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                         updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                         deleted_at TIMESTAMP
);

CREATE INDEX idx_tenants_api_key_prefix ON tenants(api_key_prefix);
CREATE INDEX idx_tenants_email ON tenants(email);

-- Usuários finais (do app do dev)
CREATE TABLE end_users (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                           external_id VARCHAR(255) NOT NULL, -- ID do user no sistema do dev
                           metadata JSONB DEFAULT '{}',
                           created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                           UNIQUE(tenant_id, external_id)
);

CREATE INDEX idx_end_users_tenant ON end_users(tenant_id);

-- Memórias (o core do produto)
CREATE TABLE memories (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                          end_user_id UUID NOT NULL REFERENCES end_users(id) ON DELETE CASCADE,
                          content TEXT NOT NULL,              -- texto da memória
                          category VARCHAR(50),               -- fact, preference, event, ...
                          source VARCHAR(50) NOT NULL,        -- message, document, note, ...
                          source_id VARCHAR(255),             -- ID no sistema do dev (opcional)
                          importance SMALLINT DEFAULT 5,      -- 1-10, pra ranking
                          embedding vector(768),              -- nomic-embed-text tem 768 dims
                          metadata JSONB DEFAULT '{}',
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          deleted_at TIMESTAMP
);

CREATE INDEX idx_memories_tenant_user ON memories(tenant_id, end_user_id);
CREATE INDEX idx_memories_category ON memories(category);
CREATE INDEX idx_memories_source ON memories(source);
-- Índice vetorial (IVFFlat) - criado depois que tiver dados, mas deixamos preparado
-- CREATE INDEX ON memories USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);