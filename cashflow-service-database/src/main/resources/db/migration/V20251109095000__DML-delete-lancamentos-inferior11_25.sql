DELETE FROM eventos.repasse_lancamento rl
USING eventos.repasse r
WHERE rl.repasse_id = r.id
  AND r.created_at < DATE '2025-11-01';

DELETE FROM eventos.repasse r
WHERE NOT EXISTS (
    SELECT 1
    FROM eventos.repasse_lancamento rl
    WHERE rl.repasse_id = r.id
);

DELETE FROM eventos.lancamento l
WHERE l.data_inicial_transacao < DATE '2025-11-01';

DELETE FROM eventos.movimento_api ma
WHERE ma.data_leitura < DATE '2025-11-01';
