CREATE TABLE eventos.parametro (
    id UUID PRIMARY KEY,
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

    -- Regra 1: apenas um valor pode ser preenchido
    CONSTRAINT chk_somente_um_valor CHECK (
        (
            (valor_texto IS NOT NULL)::int +
            (valor_inteiro IS NOT NULL)::int +
            (valor_decimal IS NOT NULL)::int
        ) = 1
    ),

    -- Regra 2: coerência entre tipo e campo preenchido
    CONSTRAINT chk_tipo_valor CHECK (
           (tipo = 'STRING'  AND valor_texto   IS NOT NULL AND valor_inteiro IS NULL AND valor_decimal IS NULL)
        OR (tipo = 'INTEGER' AND valor_inteiro IS NOT NULL AND valor_texto   IS NULL AND valor_decimal IS NULL)
        OR (tipo = 'DOUBLE'  AND valor_decimal IS NOT NULL AND valor_texto IS NULL AND valor_inteiro IS NULL)
    )
);

CREATE UNIQUE INDEX idx_parametros_chave ON eventos.parametro (chave);
CREATE INDEX idx_parametros_valor_inteiro ON eventos.parametro (valor_inteiro);
CREATE INDEX idx_parametros_valor_decimal ON eventos.parametro (valor_decimal);
