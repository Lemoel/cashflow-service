CREATE INDEX IF NOT EXISTS idx_repasse_lancamento_lookup
ON eventos.repasse_lancamento(lancamento_id, id);

COMMENT ON INDEX eventos.idx_repasse_lancamento_lookup IS
'Índice composto para otimizar LEFT JOINs nas queries de busca. Melhora performance ao verificar se lançamento já foi repassado.';