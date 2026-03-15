SET search_path TO "${tenant_schema}";

CREATE TABLE departamento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES core.tenants(id) ON DELETE RESTRICT,
    nome VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    creation_user_id VARCHAR(255),
    mod_user_id VARCHAR(255),
    CONSTRAINT uk_departamento_tenant_nome UNIQUE (tenant_id, nome)
);

CREATE INDEX idx_departamento_tenant_id ON departamento(tenant_id);
