ALTER TABLE eventos.maquina ADD COLUMN departamento_id UUID NULL;

ALTER TABLE eventos.maquina ADD CONSTRAINT fk_maquina_departamento
    FOREIGN KEY (departamento_id) REFERENCES eventos.departamento(id);

CREATE INDEX idx_maquina_departamento_id ON eventos.maquina(departamento_id);
