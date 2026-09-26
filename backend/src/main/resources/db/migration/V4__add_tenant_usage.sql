-- Uso mensal por tenant
CREATE TABLE tenant_usage (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                              period VARCHAR(7) NOT NULL,   -- formato 'YYYY-MM' (ex: '2026-09')

                              memories_count BIGINT NOT NULL DEFAULT 0,
                              tokens_used BIGINT NOT NULL DEFAULT 0,
                              extractions_count BIGINT NOT NULL DEFAULT 0,
                              searches_count BIGINT NOT NULL DEFAULT 0,
                              api_calls_count BIGINT NOT NULL DEFAULT 0,

                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

                              UNIQUE(tenant_id, period)
);

CREATE INDEX idx_tenant_usage_tenant ON tenant_usage(tenant_id);
CREATE INDEX idx_tenant_usage_period ON tenant_usage(period);

-- Adiciona campos de limites no tenant (planos já existem, mas limites explícitos ajudam)
ALTER TABLE tenants
    ADD COLUMN memory_quota BIGINT NOT NULL DEFAULT 1000,
    ADD COLUMN token_quota BIGINT NOT NULL DEFAULT 50000,
    ADD COLUMN rate_limit_per_minute INTEGER NOT NULL DEFAULT 100;