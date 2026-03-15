SET search_path TO "${tenant_schema}";

CREATE TABLE banco (
    id UUID PRIMARY KEY,
    nome VARCHAR(200),
    codigo VARCHAR(10) NOT NULL UNIQUE,
    endereco_completo TEXT NOT NULL,
    tipo_integracao VARCHAR(20) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

INSERT INTO banco (id, nome, codigo, endereco_completo, tipo_integracao)
VALUES (
    'a1b2c3d4-e5f6-7890-1234-567890abcdef',
    'PagBank',
    '290',
    'Av. Brig. Faria Lima, 1384 - Jardim Paulistano, São Paulo - SP, 01452-002',
    'API'
);
