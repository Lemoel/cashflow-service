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
    uf CHAR(2) NOT NULL,
    cep VARCHAR(9) NOT NULL,
    telefone VARCHAR(20),
    email VARCHAR(100),
    ativo BOOLEAN DEFAULT TRUE,
    creation_user_id VARCHAR(255) NOT NULL,
    mod_user_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    schema_name VARCHAR(100) NOT NULL
);

CREATE INDEX idx_tenants_cnpj ON core.tenants(cnpj);

CREATE TABLE core.user_tenant_map (
    email VARCHAR(255) PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES core.tenants(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_tenant_map_tenant_id ON core.user_tenant_map(tenant_id);

