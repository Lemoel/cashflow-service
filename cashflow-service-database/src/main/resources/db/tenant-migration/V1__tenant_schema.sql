CREATE TABLE banco (
    id UUID PRIMARY KEY,
    nome VARCHAR(200),
    codigo VARCHAR(10) NOT NULL UNIQUE,
    endereco_completo TEXT NOT NULL,
    tipo_integracao VARCHAR(20) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE TABLE congregacao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES core.tenants(id) ON DELETE RESTRICT,
    setorial_id UUID,
    nome VARCHAR(255),
    cnpj VARCHAR(14),
    logradouro VARCHAR(100) NOT NULL,
    bairro VARCHAR(50) NOT NULL,
    numero VARCHAR(10) NOT NULL,
    cidade VARCHAR(60) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    cep VARCHAR(9) NOT NULL,
    email VARCHAR(255),
    telefone VARCHAR(20),
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    creation_user_id VARCHAR(255) NOT NULL,
    mod_user_id VARCHAR(255),
    FOREIGN KEY (setorial_id) REFERENCES congregacao(id) ON DELETE RESTRICT
);

CREATE INDEX idx_congregacao_tenant_id ON congregacao(tenant_id);
CREATE INDEX idx_congregacao_setorial_id ON congregacao(setorial_id);
CREATE INDEX idx_congregacao_cnpj ON congregacao(cnpj);

CREATE TABLE departamento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES core.tenants(id) ON DELETE RESTRICT,
    nome VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    creation_user_id VARCHAR(255),
    mod_user_id VARCHAR(255),
    CONSTRAINT uk_departamento_tenant_nome UNIQUE (tenant_id, nome)
);

CREATE INDEX idx_departamento_tenant_id ON departamento(tenant_id);

CREATE TABLE acesso (
    email VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(255),
    telefone VARCHAR(20),
    ativo BOOLEAN DEFAULT TRUE,
    tipo_acesso VARCHAR(20) NOT NULL DEFAULT 'USER',
    data TIMESTAMP NOT NULL,
    mod_date_time TIMESTAMP
);

CREATE TABLE acesso_congregacao (
    email VARCHAR(255) NOT NULL,
    congregacao_id UUID NOT NULL,
    PRIMARY KEY (email, congregacao_id),
    CONSTRAINT fk_acesso_cong_acesso FOREIGN KEY (email) REFERENCES acesso(email) ON DELETE RESTRICT,
    CONSTRAINT fk_acesso_cong_congregacao FOREIGN KEY (congregacao_id) REFERENCES congregacao(id) ON DELETE RESTRICT
);

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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    creation_user_id VARCHAR(255) NOT NULL,
    mod_user_id VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maquina_id) REFERENCES maquina(id) ON DELETE SET NULL,
    FOREIGN KEY (congregacao_id) REFERENCES congregacao(id) ON DELETE SET NULL,
    FOREIGN KEY (departamento_id) REFERENCES departamento(id) ON DELETE SET NULL
);

CREATE INDEX idx_lancamento_numero_serie_leitor ON lancamento(numero_serie_leitor) WHERE numero_serie_leitor IS NOT NULL AND numero_serie_leitor != '';
CREATE INDEX idx_lancamento_maquina_id ON lancamento(maquina_id);
CREATE INDEX idx_lancamento_congregacao_id ON lancamento(congregacao_id);
CREATE INDEX idx_lancamento_departamento_id ON lancamento(departamento_id);

CREATE TABLE repasse (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maquina_id UUID REFERENCES maquina(id) ON DELETE RESTRICT,
    congregacao_id UUID REFERENCES congregacao(id) ON DELETE RESTRICT,
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

CREATE TABLE repasse_lancamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repasse_id UUID NOT NULL REFERENCES repasse(id) ON DELETE RESTRICT,
    lancamento_id UUID NOT NULL REFERENCES lancamento(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_repasse_lancamento UNIQUE (repasse_id, lancamento_id),
    CONSTRAINT uk_repasse_lancamento_lancamento UNIQUE (lancamento_id)
);

CREATE INDEX idx_repasse_maquina_id ON repasse(maquina_id);
CREATE INDEX idx_repasse_congregacao_id ON repasse(congregacao_id);
CREATE INDEX idx_repasse_data_repasse ON repasse(data_repasse);
CREATE INDEX idx_repasse_status ON repasse(status);
CREATE INDEX idx_repasse_lancamento_repasse_id ON repasse_lancamento(repasse_id);
CREATE INDEX idx_repasse_lancamento_lancamento_id ON repasse_lancamento(lancamento_id);
CREATE INDEX idx_repasse_lancamento_lookup ON repasse_lancamento(lancamento_id, id);

CREATE TABLE maquina_historico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maquina_id UUID NOT NULL REFERENCES maquina(id) ON DELETE RESTRICT,
    congregacao_id UUID REFERENCES congregacao(id) ON DELETE RESTRICT,
    departamento_id UUID REFERENCES departamento(id) ON DELETE RESTRICT,
    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_fim TIMESTAMP WITH TIME ZONE NULL
);

CREATE INDEX idx_maquina_historico_maquina_id ON maquina_historico(maquina_id);
CREATE INDEX idx_maquina_historico_maquina_data ON maquina_historico(maquina_id, data_inicio);

CREATE TABLE parametro (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chave VARCHAR(100) NOT NULL UNIQUE,
    valor_texto TEXT,
    valor_inteiro BIGINT,
    valor_decimal DOUBLE PRECISION,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('STRING', 'INTEGER', 'DOUBLE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN NOT NULL DEFAULT FALSE,
    creation_user_id VARCHAR(255) NOT NULL,
    mod_user_id VARCHAR(255),
    CONSTRAINT chk_somente_um_valor CHECK (
        ( (valor_texto IS NOT NULL)::int + (valor_inteiro IS NOT NULL)::int + (valor_decimal IS NOT NULL)::int ) = 1
    ),
    CONSTRAINT chk_tipo_valor CHECK (
           (tipo = 'STRING'  AND valor_texto IS NOT NULL AND valor_inteiro IS NULL AND valor_decimal IS NULL)
        OR (tipo = 'INTEGER' AND valor_inteiro IS NOT NULL AND valor_texto IS NULL AND valor_decimal IS NULL)
        OR (tipo = 'DOUBLE'  AND valor_decimal IS NOT NULL AND valor_texto IS NULL AND valor_inteiro IS NULL)
    )
);

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

INSERT INTO banco (id, nome, codigo, endereco_completo, tipo_integracao)
VALUES (
    'a1b2c3d4-e5f6-7890-1234-567890abcdef',
    'PagBank',
    '290',
    'Av. Brig. Faria Lima, 1384 - Jardim Paulistano, São Paulo - SP, 01452-002',
    'API'
);
