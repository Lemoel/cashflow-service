CREATE TABLE eventos.maquina_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maquina_id UUID NOT NULL REFERENCES eventos.maquina(id),
    congregacao_id UUID NULL REFERENCES eventos.congregacao(id),
    departamento_id UUID NULL REFERENCES eventos.departamento(id),
    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_fim TIMESTAMP WITH TIME ZONE NULL
);

CREATE INDEX idx_maquina_historico_maquina_id ON eventos.maquina_historico(maquina_id);

CREATE INDEX idx_maquina_historico_maquina_data ON eventos.maquina_historico(maquina_id, data_inicio);
