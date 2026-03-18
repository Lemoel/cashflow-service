CREATE SCHEMA IF NOT EXISTS core;

CREATE TABLE core.tenants (
    id UUID PRIMARY KEY,
    cnpj VARCHAR(14) NOT NULL UNIQUE,
    nome_fantasia VARCHAR(100) NOT NULL,
    razao_social VARCHAR(150),
    logradouro VARCHAR(100) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    complemento VARCHAR(50),
    bairro VARCHAR(50),
    cidade VARCHAR(60) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    cep VARCHAR(9) NOT NULL,
    telefone VARCHAR(20),
    email VARCHAR(100),
    ativo BOOLEAN DEFAULT TRUE,
    created_by_id VARCHAR(113) NOT NULL,
    dti_created_date TIMESTAMP NOT NULL,
    last_modified_by_id VARCHAR(113) NOT NULL,
    dti_last_modified_date TIMESTAMP NOT NULL,
    schema_name VARCHAR(100) NOT NULL
);

CREATE INDEX idx_tenants_cnpj ON core.tenants(cnpj);

CREATE TABLE core.user_tenant_map (
    email VARCHAR(255) PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES core.tenants(id) ON DELETE RESTRICT,
    created_by_id VARCHAR(113) NOT NULL,
    dti_created_date TIMESTAMP NOT NULL,
    last_modified_by_id VARCHAR(113) NOT NULL,
    dti_last_modified_date TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_tenant_map_tenant_id ON core.user_tenant_map(tenant_id);

CREATE INDEX idx_tenants_ativo_nome_fantasia
    ON core.tenants(ativo, nome_fantasia)
    WHERE ativo = true;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_tenants_nome_fantasia_trgm
    ON core.tenants
    USING gin (LOWER(nome_fantasia) gin_trgm_ops);
