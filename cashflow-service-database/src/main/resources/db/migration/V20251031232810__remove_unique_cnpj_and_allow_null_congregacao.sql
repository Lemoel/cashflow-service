-- Remove a constraint UNIQUE do CNPJ e permite valores NULL na tabela congregacao
-- O CNPJ agora pode ser NULL e permite valores duplicados

-- PostgreSQL cria automaticamente uma constraint UNIQUE quando você usa UNIQUE na definição da coluna
-- O nome padrão é geralmente {tabela}_{coluna}_key
-- Devemos remover a constraint primeiro, o que automaticamente remove o índice único associado
ALTER TABLE eventos.congregacao 
DROP CONSTRAINT IF EXISTS congregacao_cnpj_key;

-- Tentar outros nomes possíveis que podem ter sido criados
ALTER TABLE eventos.congregacao 
DROP CONSTRAINT IF EXISTS uk_cnpj_congregacao;

ALTER TABLE eventos.congregacao 
DROP CONSTRAINT IF EXISTS congregacao_cnpj_unique;

-- Permitir NULL no CNPJ
ALTER TABLE eventos.congregacao 
ALTER COLUMN cnpj DROP NOT NULL;

-- Recriar índice normal (não único) para melhor performance em consultas
-- Nota: já existe um índice idx_congregacao_cnpj criado na migration inicial, 
-- mas vamos garantir que não seja único (caso tenha sido criado como único)
DROP INDEX IF EXISTS eventos.idx_congregacao_cnpj;
CREATE INDEX idx_congregacao_cnpj ON eventos.congregacao(cnpj);

