SET search_path TO "${tenant_schema}";

CREATE TABLE parametro (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chave VARCHAR(100) NOT NULL UNIQUE,
    valor_texto TEXT,
    valor_inteiro BIGINT,
    valor_decimal DOUBLE PRECISION,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('STRING', 'INTEGER', 'DOUBLE')),
    ativo BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_id VARCHAR(113) NOT NULL,
    dti_created_date TIMESTAMP NOT NULL,
    last_modified_by_id VARCHAR(113) NOT NULL,
    dti_last_modified_date TIMESTAMP NOT NULL,
    CONSTRAINT chk_somente_um_valor CHECK (
        ( (valor_texto IS NOT NULL)::int + (valor_inteiro IS NOT NULL)::int + (valor_decimal IS NOT NULL)::int ) = 1
    ),
    CONSTRAINT chk_tipo_valor CHECK (
           (tipo = 'STRING'  AND valor_texto IS NOT NULL AND valor_inteiro IS NULL AND valor_decimal IS NULL)
        OR (tipo = 'INTEGER' AND valor_inteiro IS NOT NULL AND valor_texto IS NULL AND valor_decimal IS NULL)
        OR (tipo = 'DOUBLE'  AND valor_decimal IS NOT NULL AND valor_texto IS NULL AND valor_inteiro IS NULL)
    )
);
