-- Flyway Migration: Ajuste de integridade referencial para RESTRICT
-- Descrição: Padronização de todas as FKs para impedir deleção acidental de registros vinculados.

-- 1. Tabela: repasse_lancamento
ALTER TABLE eventos.repasse_lancamento DROP CONSTRAINT IF EXISTS fk_repasse_lancamento_repasse;
ALTER TABLE eventos.repasse_lancamento ADD CONSTRAINT fk_repasse_lancamento_repasse
    FOREIGN KEY (repasse_id) REFERENCES eventos.repasse(id) ON DELETE RESTRICT;

ALTER TABLE eventos.repasse_lancamento DROP CONSTRAINT IF EXISTS fk_repasse_lancamento_lancamento;
ALTER TABLE eventos.repasse_lancamento ADD CONSTRAINT fk_repasse_lancamento_lancamento
    FOREIGN KEY (lancamento_id) REFERENCES eventos.lancamento(id) ON DELETE RESTRICT;

COMMENT ON CONSTRAINT fk_repasse_lancamento_repasse ON eventos.repasse_lancamento IS 'Impede apagar o repasse se houver lançamentos vinculados.';
COMMENT ON CONSTRAINT fk_repasse_lancamento_lancamento ON eventos.repasse_lancamento IS 'Impede apagar o lancamento se houver registros de repasse vinculados.';

-- 2. Tabela: congregacao
ALTER TABLE eventos.congregacao DROP CONSTRAINT IF EXISTS congregacao_setorial_id_fkey;
ALTER TABLE eventos.congregacao ADD CONSTRAINT congregacao_setorial_id_fkey
    FOREIGN KEY (setorial_id) REFERENCES eventos.congregacao(id) ON DELETE RESTRICT;

ALTER TABLE eventos.congregacao DROP CONSTRAINT IF EXISTS congregacao_tenant_id_fkey;
ALTER TABLE eventos.congregacao ADD CONSTRAINT congregacao_tenant_id_fkey
    FOREIGN KEY (tenant_id) REFERENCES core.tenants(id) ON DELETE RESTRICT;

-- 3. Tabela: maquina
ALTER TABLE eventos.maquina DROP CONSTRAINT IF EXISTS maquina_congregacao_id_fkey;
ALTER TABLE eventos.maquina ADD CONSTRAINT maquina_congregacao_id_fkey
    FOREIGN KEY (congregacao_id) REFERENCES eventos.congregacao(id) ON DELETE RESTRICT;

ALTER TABLE eventos.maquina DROP CONSTRAINT IF EXISTS maquina_banco_id_fkey;
ALTER TABLE eventos.maquina ADD CONSTRAINT maquina_banco_id_fkey
    FOREIGN KEY (banco_id) REFERENCES eventos.banco(id) ON DELETE RESTRICT;

-- 4. Tabela: lancamento
ALTER TABLE eventos.lancamento DROP CONSTRAINT IF EXISTS lancamento_maquina_id_fkey;
ALTER TABLE eventos.lancamento ADD CONSTRAINT lancamento_maquina_id_fkey
    FOREIGN KEY (maquina_id) REFERENCES eventos.maquina(id) ON DELETE RESTRICT;

-- 5. Tabela: ACESSO_CONGREGACAO
ALTER TABLE eventos.ACESSO_CONGREGACAO DROP CONSTRAINT IF EXISTS fk_acesso;
ALTER TABLE eventos.ACESSO_CONGREGACAO ADD CONSTRAINT fk_acesso
    FOREIGN KEY (email) REFERENCES eventos.acesso(email) ON DELETE RESTRICT;

ALTER TABLE eventos.ACESSO_CONGREGACAO DROP CONSTRAINT IF EXISTS fk_congregacao;
ALTER TABLE eventos.ACESSO_CONGREGACAO ADD CONSTRAINT fk_congregacao
    FOREIGN KEY (congregacao_id) REFERENCES eventos.congregacao(id) ON DELETE RESTRICT;