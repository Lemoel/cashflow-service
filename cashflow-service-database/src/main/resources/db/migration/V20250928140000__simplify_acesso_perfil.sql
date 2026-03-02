ALTER TABLE eventos.acesso
ADD COLUMN ativo BOOLEAN DEFAULT true NOT NULL;

ALTER TABLE eventos.acesso
ADD COLUMN tipo_acesso VARCHAR(20) NOT NULL DEFAULT 'USER';

UPDATE eventos.acesso SET ativo = true WHERE ativo IS NULL;

UPDATE eventos.acesso 
SET tipo_acesso = (
    SELECT p.tipo_acesso 
    FROM eventos.perfil p 
    WHERE p.email = eventos.acesso.email
);

UPDATE eventos.acesso 
SET tipo_acesso = 'ADMIN' 
WHERE tipo_acesso IS NULL;

DROP TABLE eventos.perfil;