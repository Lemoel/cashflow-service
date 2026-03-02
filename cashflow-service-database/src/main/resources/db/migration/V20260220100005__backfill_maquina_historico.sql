INSERT INTO eventos.maquina_historico (id, maquina_id, congregacao_id, departamento_id, data_inicio, data_fim)
SELECT gen_random_uuid(), m.id, m.congregacao_id, m.departamento_id, m.created_at, NULL
FROM eventos.maquina m;
