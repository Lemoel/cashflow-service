SET search_path TO "${tenant_schema}";

CREATE TABLE movimento_api (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payload TEXT,
    status VARCHAR(30) NOT NULL,
    pagina NUMERIC(2) NOT NULL,
    total_elementos NUMERIC(4),
    total_paginas NUMERIC(4),
    data_leitura DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    creation_user_id VARCHAR(255) NOT NULL,
    mod_user_id VARCHAR(255),
    CONSTRAINT uk_movimento_data_pagina UNIQUE (data_leitura, pagina)
);

CREATE INDEX idx_movimento_api_status ON movimento_api(status);
