CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_repasse_lancamento_lancamento_id
ON eventos.repasse_lancamento(lancamento_id);

COMMENT ON INDEX eventos.idx_uq_repasse_lancamento_lancamento_id IS
'Garante que um lançamento só pode ser incluído em UM único repasse. Previne duplicação de retiradas e garante integridade financeira.';