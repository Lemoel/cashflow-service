SET search_path TO "${tenant_schema}";

CREATE TABLE maquina_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maquina_id UUID NOT NULL REFERENCES maquina(id) ON DELETE RESTRICT,
    congregacao_id UUID REFERENCES congregacao(id) ON DELETE RESTRICT,
    departamento_id UUID REFERENCES departamento(id) ON DELETE RESTRICT,
    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_fim TIMESTAMP WITH TIME ZONE NULL,
    created_by_id VARCHAR(113) NOT NULL,
    dti_created_date TIMESTAMP NOT NULL,
    last_modified_by_id VARCHAR(113) NOT NULL,
    dti_last_modified_date TIMESTAMP NOT NULL
);

CREATE INDEX idx_maquina_historico_maquina_id ON maquina_historico(maquina_id);
CREATE INDEX idx_maquina_historico_maquina_data ON maquina_historico(maquina_id, data_inicio);
CREATE INDEX idx_maquina_historico_open ON maquina_historico(maquina_id) WHERE data_fim IS NULL;
