SET search_path TO "${tenant_schema}";

CREATE TABLE repasse (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maquina_id UUID REFERENCES maquina(id) ON DELETE RESTRICT,
    congregacao_id UUID REFERENCES congregacao(id) ON DELETE RESTRICT,
    data_repasse TIMESTAMPTZ NOT NULL,
    nome_representante VARCHAR(255) NOT NULL,
    observacoes TEXT,
    valor_repasse DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    valor_total_transacoes DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    valor_total_taxas DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    quantidade_lancamentos INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PROCESSADO',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    creation_user_id VARCHAR(255) NOT NULL,
    mod_user_id VARCHAR(255)
);

CREATE INDEX idx_repasse_maquina_id ON repasse(maquina_id);
CREATE INDEX idx_repasse_congregacao_id ON repasse(congregacao_id);
CREATE INDEX idx_repasse_data_repasse ON repasse(data_repasse);
CREATE INDEX idx_repasse_status ON repasse(status);
