CREATE SCHEMA IF NOT EXISTS core;

CREATE TABLE core.tenants (
                         id UUID PRIMARY KEY,
                         cnpj VARCHAR(14) NOT NULL UNIQUE,
                         nome_fantasia VARCHAR(100) NOT NULL,
                         razao_social VARCHAR(150),
                         logradouro VARCHAR(100) NOT NULL,
                         numero VARCHAR(10) NOT NULL,
                         complemento VARCHAR(50),
                         bairro VARCHAR(50),
                         cidade VARCHAR(60) NOT NULL,
                         uf CHAR(2) NOT NULL,
                         cep VARCHAR(9) NOT NULL,
                         telefone VARCHAR(20),
                         email VARCHAR(100),
                         ativo BOOLEAN DEFAULT TRUE,
                         creation_user_id VARCHAR(255) NOT NULL,
                         mod_user_id VARCHAR(255),
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE SCHEMA IF NOT EXISTS eventos;

-- Criação das tabelas do sistema de pagamentos

-- Tabela de Máquinas
CREATE TABLE eventos.banco (
                          id UUID PRIMARY KEY,
                          nome varchar(200),
                          codigo VARCHAR(10) NOT NULL UNIQUE,
                          endereco_completo TEXT NOT NULL,
                          tipo_integracao varchar(20) NOT NULL,
                          ativo BOOLEAN DEFAULT TRUE
);

-- Tabela de Hierarquia de Visualizações
CREATE TABLE eventos.congregacao (
                                         id          UUID PRIMARY KEY,
                                         tenant_id   UUID NOT NULL,
                                         matriz_id   UUID,
                                         setorial_id UUID,
                                         nome        VARCHAR(255),
                                         cnpj        VARCHAR(14) NOT NULL UNIQUE,
                                         logradouro  VARCHAR(255) NOT NULL,
                                         bairro      VARCHAR(255) NOT NULL,
                                         numero      VARCHAR(20) NOT NULL,
                                         cidade      VARCHAR(255) NOT NULL,
                                         uf          VARCHAR(2) NOT NULL,
                                         cep         VARCHAR(9) NOT NULL,
                                         email       VARCHAR(255),
                                         telefone    VARCHAR(20),
                                         ativo BOOLEAN DEFAULT TRUE,
                                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                         creation_user_id VARCHAR(255) NOT NULL,
                                         mod_user_id VARCHAR(255),
                                         FOREIGN KEY (matriz_id) REFERENCES eventos.congregacao(id) ON DELETE CASCADE,
                                         FOREIGN KEY (setorial_id) REFERENCES eventos.congregacao(id) ON DELETE CASCADE,
                                         FOREIGN KEY (tenant_id) REFERENCES core.tenants(id) ON DELETE CASCADE
);

-- Índices para chaves estrangeiras da tabela congregacao
CREATE INDEX idx_congregacao_tenant_id ON eventos.congregacao(tenant_id);
CREATE INDEX idx_congregacao_matriz_id ON eventos.congregacao(matriz_id);
CREATE INDEX idx_congregacao_setorial_id ON eventos.congregacao(setorial_id);

CREATE TABLE eventos.maquina (
  id UUID PRIMARY KEY,
  numero_serie_leitor VARCHAR(20) UNIQUE,
  congregacao_id UUID,
  banco_id UUID NOT NULL,
  ativo BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  version BIGINT DEFAULT 0,
  creation_user_id VARCHAR(255) NOT NULL,
  mod_user_id VARCHAR(255),
  setor_desc VARCHAR(255) NULL,
  FOREIGN KEY (congregacao_id) REFERENCES eventos.congregacao(id) ON DELETE CASCADE,
  FOREIGN KEY (banco_id) REFERENCES eventos.banco(id) ON DELETE CASCADE
);

-- Índices para chaves estrangeiras da tabela maquina
CREATE INDEX idx_maquina_congregacao_id ON eventos.maquina(congregacao_id);
CREATE INDEX idx_maquina_banco_id ON eventos.maquina(banco_id);
CREATE INDEX idx_maquina_numero_serie_leitor ON eventos.maquina (numero_serie_leitor) WHERE numero_serie_leitor IS NOT NULL AND numero_serie_leitor != '';

CREATE TABLE IF NOT EXISTS eventos.lancamento (id UUID PRIMARY KEY,
                                                   nsu VARCHAR(30),
                                                   tid VARCHAR(64),
                                                   codigo_transacao VARCHAR(33),
                                                   parcela VARCHAR(5) NOT NULL,
                                                   tipo_evento VARCHAR(2) NOT NULL,
                                                   meio_captura VARCHAR(2) NOT NULL,
                                                   valor_parcela NUMERIC(12, 2) NOT NULL,
                                                   meio_pagamento VARCHAR(2) NOT NULL,
                                                   estabelecimento VARCHAR(20) NOT NULL,
                                                   pagamento_prazo CHAR(1) NOT NULL,
                                                   taxa_intermediacao NUMERIC(12, 2) NOT NULL,
                                                   numero_serie_leitor VARCHAR(20),
                                                   maquina_id UUID,
                                                   valor_total_transacao NUMERIC(12, 2) NOT NULL,
                                                   data_inicial_transacao DATE NOT NULL,
                                                   hora_inicial_transacao VARCHAR(8) NOT NULL,
                                                   data_prevista_pagamento DATE NOT NULL,
                                                   valor_liquido_transacao NUMERIC(12, 2) NOT NULL,
                                                   valor_original_transacao NUMERIC(12, 2) NOT NULL,
                                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                                   creation_user_id VARCHAR(255) NOT NULL,
                                                   mod_user_id VARCHAR(255),
                                                   updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      FOREIGN KEY (maquina_id) REFERENCES eventos.maquina(id) ON DELETE SET NULL);

CREATE UNIQUE INDEX uk_lancamento_nsu_parcela_codigo_transacao ON eventos.lancamento(nsu, tid, parcela, codigo_transacao) WHERE nsu IS NOT NULL AND nsu != '' AND tid != '' AND parcela != '' AND codigo_transacao != '';
CREATE INDEX idx_lancamento_numero_serie_leitor ON eventos.lancamento(numero_serie_leitor) WHERE numero_serie_leitor IS NOT NULL AND numero_serie_leitor != '';

-- Índices adicionais para campos comumente usados em consultas
CREATE INDEX idx_tenants_cnpj ON core.tenants(cnpj);
CREATE INDEX idx_banco_codigo ON eventos.banco(codigo);
CREATE INDEX idx_congregacao_cnpj ON eventos.congregacao(cnpj);

INSERT INTO eventos.banco (id, nome, codigo, endereco_completo, tipo_integracao)
VALUES ('a1b2c3d4-e5f6-7890-1234-567890abcdef',
    'PagBank',
    '290',
    'Av. Brig. Faria Lima, 1384 - Jardim Paulistano, São Paulo - SP, 01452-002',
    'API');