UPDATE eventos.lancamento l
SET congregacao_id = m.congregacao_id, departamento_id = m.departamento_id
FROM eventos.maquina m
WHERE l.maquina_id = m.id AND l.maquina_id IS NOT NULL;
