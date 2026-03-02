-- Remove a coluna matriz_id e seus relacionamentos da tabela congregacao
-- A congregação sem setorial é automaticamente uma matriz

-- Remover índice da coluna matriz_id
DROP INDEX IF EXISTS eventos.idx_congregacao_matriz_id;

-- Remover foreign key (tentar nomes comuns que o PostgreSQL pode ter gerado)
ALTER TABLE eventos.congregacao 
DROP CONSTRAINT IF EXISTS congregacao_matriz_id_fkey;

ALTER TABLE eventos.congregacao 
DROP CONSTRAINT IF EXISTS congregacao_congregacao_matriz_id_fkey;

-- Remover coluna matriz_id
ALTER TABLE eventos.congregacao 
DROP COLUMN IF EXISTS matriz_id;

