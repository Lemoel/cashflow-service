SET search_path TO "${tenant_schema}";

CREATE TABLE departamento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES core.tenants(id) ON DELETE RESTRICT,
    nome VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    created_by_id VARCHAR(113) NOT NULL,
    dti_created_date TIMESTAMP NOT NULL,
    last_modified_by_id VARCHAR(113) NOT NULL,
    dti_last_modified_date TIMESTAMP NOT NULL,
    CONSTRAINT uk_departamento_tenant_nome UNIQUE (tenant_id, nome)
);

CREATE INDEX idx_departamento_tenant_id ON departamento(tenant_id);
