INSERT INTO eventos.ACESSO (email, password, data, MOD_DATE_TIME) VALUES
 ('financeiro@eventosad.com', 'password1', NOW(), NULL);

INSERT INTO eventos.PERFIL (email, tipo_acesso)
VALUES ('financeiro@eventosad.com', 'ADMIN');