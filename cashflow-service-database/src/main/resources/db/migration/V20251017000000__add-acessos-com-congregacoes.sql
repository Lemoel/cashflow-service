CREATE TABLE eventos.ACESSO_CONGREGACAO (
    email VARCHAR(255)  NOT NULL,
    congregacao_id UUID NOT NULL,
    PRIMARY KEY (email, congregacao_id),
    CONSTRAINT fk_acesso FOREIGN KEY (email)
        REFERENCES eventos.acesso (email) ON DELETE CASCADE,
    CONSTRAINT fk_congregacao FOREIGN KEY (congregacao_id)
        REFERENCES eventos.congregacao (id) ON DELETE CASCADE
);