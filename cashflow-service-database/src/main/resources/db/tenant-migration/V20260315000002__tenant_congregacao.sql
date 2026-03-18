SET search_path TO "${tenant_schema}";

CREATE TABLE congregacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES core.tenants(id) ON DELETE RESTRICT,
    setorial_id UUID,
    nome VARCHAR(255),
    cnpj VARCHAR(14),
    logradouro VARCHAR(255) NOT NULL,
    bairro VARCHAR(255) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    cidade VARCHAR(255) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    cep VARCHAR(9) NOT NULL,
    email VARCHAR(255),
    telefone VARCHAR(20),
    ativo BOOLEAN DEFAULT TRUE,
    created_by_id VARCHAR(113) NOT NULL,
    dti_created_date TIMESTAMP NOT NULL,
    last_modified_by_id VARCHAR(113) NOT NULL,
    dti_last_modified_date TIMESTAMP NOT NULL,
    FOREIGN KEY (setorial_id) REFERENCES congregacao(id) ON DELETE RESTRICT
);

CREATE INDEX idx_congregacao_tenant_id ON congregacao(tenant_id);
CREATE INDEX idx_congregacao_setorial_id ON congregacao(setorial_id);
CREATE INDEX idx_congregacao_cnpj ON congregacao(cnpj);
