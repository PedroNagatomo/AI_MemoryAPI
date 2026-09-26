ALTER TABLE tenants
    ADD COLUMN environment VARCHAR(10) NOT NULL DEFAULT 'TEST';

CREATE INDEX idx_tenants_env ON tenants(environment);