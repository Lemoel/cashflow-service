CREATE TABLE eventos.repasse (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maquina_id UUID NOT NULL,
    congregacao_id UUID,
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

CREATE TABLE eventos.repasse_lancamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repasse_id UUID NOT NULL,
    lancamento_id UUID NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_repasse_lancamento_repasse FOREIGN KEY (repasse_id) REFERENCES eventos.repasse(id) ON DELETE CASCADE,
    CONSTRAINT fk_repasse_lancamento_lancamento FOREIGN KEY (lancamento_id) REFERENCES eventos.lancamento(id) ON DELETE CASCADE,
    CONSTRAINT uk_repasse_lancamento UNIQUE (repasse_id, lancamento_id)
);

CREATE INDEX IF NOT EXISTS idx_repasse_maquina_id ON eventos.repasse(maquina_id);
CREATE INDEX IF NOT EXISTS idx_repasse_congregacao_id ON eventos.repasse(congregacao_id);
CREATE INDEX IF NOT EXISTS idx_repasse_data_repasse ON eventos.repasse(data_repasse);
CREATE INDEX IF NOT EXISTS idx_repasse_status ON eventos.repasse(status);
CREATE INDEX IF NOT EXISTS idx_repasse_lancamento_repasse_id ON eventos.repasse_lancamento(repasse_id);
CREATE INDEX IF NOT EXISTS idx_repasse_lancamento_lancamento_id ON eventos.repasse_lancamento(lancamento_id);

COMMENT ON TABLE eventos.repasse IS 'Tabela para controle de repasses/retiradas de dinheiro das máquinas';
COMMENT ON COLUMN eventos.repasse.maquina_id IS 'ID da máquina de onde foi feita a retirada';
COMMENT ON COLUMN eventos.repasse.congregacao_id IS 'ID da congregação responsável pela máquina';
COMMENT ON COLUMN eventos.repasse.data_repasse IS 'Data e hora em que o repasse foi realizado';
COMMENT ON COLUMN eventos.repasse.nome_representante IS 'Nome da pessoa que recebeu o repasse';
COMMENT ON COLUMN eventos.repasse.valor_repasse IS 'Valor líquido repassado (total - taxas)';
COMMENT ON COLUMN eventos.repasse.valor_total_transacoes IS 'Valor total das transações incluídas no repasse';
COMMENT ON COLUMN eventos.repasse.valor_total_taxas IS 'Valor total das taxas das transações incluídas';
COMMENT ON COLUMN eventos.repasse.quantidade_lancamentos IS 'Quantidade de lançamentos incluídos no repasse';
COMMENT ON COLUMN eventos.repasse.status IS 'Status do repasse (PROCESSADO, CANCELADO, etc.)';

COMMENT ON TABLE eventos.repasse_lancamento IS 'Tabela de associação entre repasses e lançamentos';
COMMENT ON COLUMN eventos.repasse_lancamento.repasse_id IS 'ID do repasse';
COMMENT ON COLUMN eventos.repasse_lancamento.lancamento_id IS 'ID do lançamento incluído no repasse';
