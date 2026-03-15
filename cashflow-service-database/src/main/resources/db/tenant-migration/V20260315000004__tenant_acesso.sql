SET search_path TO "${tenant_schema}";

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
