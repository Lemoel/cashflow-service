SET search_path TO "${tenant_schema}";

CREATE TABLE maquina (
    id UUID PRIMARY KEY,
    numero_serie_leitor VARCHAR(20) UNIQUE,
    congregacao_id UUID,
    banco_id UUID NOT NULL,
    departamento_id UUID,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    creation_user_id VARCHAR(255) NOT NULL,
    mod_user_id VARCHAR(255),
    FOREIGN KEY (congregacao_id) REFERENCES congregacao(id) ON DELETE RESTRICT,
    FOREIGN KEY (banco_id) REFERENCES banco(id) ON DELETE RESTRICT,
    FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE RESTRICT
);

CREATE INDEX idx_maquina_congregacao_id ON maquina(congregacao_id);
CREATE INDEX idx_maquina_banco_id ON maquina(banco_id);
CREATE INDEX idx_maquina_numero_serie_leitor ON maquina(numero_serie_leitor) WHERE numero_serie_leitor IS NOT NULL AND numero_serie_leitor != '';
CREATE INDEX idx_maquina_departamento_id ON maquina(departamento_id);
