SET search_path TO "${tenant_schema}";

CREATE TABLE acesso_congregacao (
    email VARCHAR(255) NOT NULL,
    congregacao_id UUID NOT NULL,
    PRIMARY KEY (email, congregacao_id),
    CONSTRAINT fk_acesso_cong_acesso FOREIGN KEY (email) REFERENCES acesso(email) ON DELETE RESTRICT,
    CONSTRAINT fk_acesso_cong_congregacao FOREIGN KEY (congregacao_id) REFERENCES congregacao(id) ON DELETE RESTRICT
);

CREATE INDEX idx_acesso_congregacao_congregacao_id ON acesso_congregacao(congregacao_id);
