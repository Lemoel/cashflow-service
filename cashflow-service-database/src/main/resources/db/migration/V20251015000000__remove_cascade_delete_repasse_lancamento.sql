-- Remove ON DELETE CASCADE da constraint fk_repasse_lancamento_repasse
-- e substitui por ON DELETE RESTRICT para prevenir deleção acidental

-- Dropar a constraint existente
ALTER TABLE eventos.repasse_lancamento 
DROP CONSTRAINT IF EXISTS fk_repasse_lancamento_repasse;

-- Recriar a constraint sem ON DELETE CASCADE
-- Usando ON DELETE RESTRICT para prevenir deleção de repasse que possui lançamentos associados
ALTER TABLE eventos.repasse_lancamento 
ADD CONSTRAINT fk_repasse_lancamento_repasse 
FOREIGN KEY (repasse_id) 
REFERENCES eventos.repasse(id) 
ON DELETE RESTRICT;

-- Comentário explicativo
COMMENT ON CONSTRAINT fk_repasse_lancamento_repasse ON eventos.repasse_lancamento 
IS 'Foreign key para repasse. ON DELETE RESTRICT previne deleção de repasse com lançamentos associados';
