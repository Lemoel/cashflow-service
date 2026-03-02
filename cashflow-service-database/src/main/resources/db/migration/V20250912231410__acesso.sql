CREATE TABLE eventos.ACESSO (email VARCHAR(255) PRIMARY KEY,
                                password VARCHAR(255) NOT NULL,
                                data TIMESTAMP NOT NULL,
                                MOD_DATE_TIME TIMESTAMP);

INSERT INTO eventos.ACESSO (email, password, data, MOD_DATE_TIME) VALUES
 ('meirezende@hotmail.com', 'password1', NOW(), NULL),
 ('lemoel@gmail.com', 'password2', NOW(), NULL);

 CREATE TABLE eventos.PERFIL (
                                email VARCHAR(255) PRIMARY KEY,
                                tipo_acesso VARCHAR(50) NOT NULL CHECK (tipo_acesso IN ('ADMIN', 'FISCAL', 'GESTOR')),
                                CONSTRAINT fk_email FOREIGN KEY (email) REFERENCES eventos.ACESSO(email)
                                    ON DELETE CASCADE
                                    ON UPDATE CASCADE
 );

 INSERT INTO eventos.PERFIL (email, tipo_acesso)
 VALUES ('meirezende@hotmail.com', 'ADMIN');

 INSERT INTO eventos.PERFIL (email, tipo_acesso)
  VALUES ('lemoel@gmail.com', 'ADMIN');