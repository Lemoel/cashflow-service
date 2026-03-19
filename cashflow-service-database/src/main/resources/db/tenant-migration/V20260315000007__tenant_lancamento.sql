SET search_path TO "${tenant_schema}";

CREATE TABLE lancamento (
    id UUID PRIMARY KEY,
    nsu VARCHAR(30),
    tid VARCHAR(64),
    codigo_transacao VARCHAR(33),
    parcela VARCHAR(5) NOT NULL,
    tipo_evento VARCHAR(2) NOT NULL,
    meio_captura VARCHAR(2) NOT NULL,
    valor_parcela NUMERIC(12,2) NOT NULL,
    meio_pagamento VARCHAR(2) NOT NULL,
    estabelecimento VARCHAR(20) NOT NULL,
    pagamento_prazo CHAR(1) NOT NULL,
    taxa_intermediacao NUMERIC(12,2) NOT NULL,
    numero_serie_leitor VARCHAR(20),
    maquina_id UUID,
    congregacao_id UUID,
    departamento_id UUID,
    valor_total_transacao NUMERIC(12,2) NOT NULL,
    data_inicial_transacao DATE NOT NULL,
    hora_inicial_transacao VARCHAR(8) NOT NULL,
    data_prevista_pagamento DATE NOT NULL,
    valor_liquido_transacao NUMERIC(12,2) NOT NULL,
    valor_original_transacao NUMERIC(12,2) NOT NULL,
    created_by_id VARCHAR(113) NOT NULL,
    dti_created_date TIMESTAMP NOT NULL,
    last_modified_by_id VARCHAR(113) NOT NULL,
    dti_last_modified_date TIMESTAMP NOT NULL,
    FOREIGN KEY (maquina_id) REFERENCES maquina(id) ON DELETE SET NULL,
    FOREIGN KEY (congregacao_id) REFERENCES congregacao(id) ON DELETE SET NULL,
    FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE SET NULL
);

CREATE INDEX idx_lancamento_numero_serie_leitor ON lancamento(numero_serie_leitor) WHERE numero_serie_leitor IS NOT NULL AND numero_serie_leitor != '';
CREATE INDEX idx_lancamento_maquina_id ON lancamento(maquina_id);
CREATE INDEX idx_lancamento_congregacao_id ON lancamento(congregacao_id);
CREATE INDEX idx_lancamento_departamento_id ON lancamento(departamento_id);
