SET search_path TO "${tenant_schema}";

CREATE TABLE repasse_lancamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repasse_id UUID NOT NULL REFERENCES repasse(id) ON DELETE RESTRICT,
    lancamento_id UUID NOT NULL REFERENCES lancamento(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_repasse_lancamento UNIQUE (repasse_id, lancamento_id),
    CONSTRAINT uk_repasse_lancamento_lancamento UNIQUE (lancamento_id)
);

CREATE INDEX idx_repasse_lancamento_repasse_id ON repasse_lancamento(repasse_id);
CREATE INDEX idx_repasse_lancamento_lancamento_id ON repasse_lancamento(lancamento_id);
CREATE INDEX idx_repasse_lancamento_lookup ON repasse_lancamento(lancamento_id, id);
