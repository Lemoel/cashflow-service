ALTER TABLE eventos.lancamento ADD COLUMN congregacao_id UUID NULL REFERENCES eventos.congregacao(id);

ALTER TABLE eventos.lancamento ADD COLUMN departamento_id UUID NULL REFERENCES eventos.departamento(id);

CREATE INDEX idx_lancamento_congregacao_id ON eventos.lancamento(congregacao_id);

CREATE INDEX idx_lancamento_departamento_id ON eventos.lancamento(departamento_id);
