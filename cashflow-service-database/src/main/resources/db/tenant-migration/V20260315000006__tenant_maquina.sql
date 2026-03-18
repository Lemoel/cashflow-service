SET search_path TO "${tenant_schema}";

CREATE TABLE maquina (
    id UUID PRIMARY KEY,
    numero_serie_leitor VARCHAR(20) UNIQUE,
    congregacao_id UUID,
    banco_id UUID NOT NULL,
    departamento_id UUID,
    ativo BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_by_id VARCHAR(113) NOT NULL,
    dti_created_date TIMESTAMP NOT NULL,
    last_modified_by_id VARCHAR(113) NOT NULL,
    dti_last_modified_date TIMESTAMP NOT NULL,
    FOREIGN KEY (congregacao_id) REFERENCES congregacao(id) ON DELETE RESTRICT,
    FOREIGN KEY (banco_id) REFERENCES banco(id) ON DELETE RESTRICT,
    FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE RESTRICT
);

CREATE INDEX idx_maquina_congregacao_id ON maquina(congregacao_id);
CREATE INDEX idx_maquina_banco_id ON maquina(banco_id);
CREATE INDEX idx_maquina_numero_serie_leitor ON maquina(numero_serie_leitor) WHERE numero_serie_leitor IS NOT NULL AND numero_serie_leitor != '';
CREATE INDEX idx_maquina_departamento_id ON maquina(departamento_id);
