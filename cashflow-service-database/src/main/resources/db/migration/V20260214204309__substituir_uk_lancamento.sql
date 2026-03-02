ALTER TABLE eventos.lancamento
DROP CONSTRAINT IF EXISTS uk_lancamento_nsu_parcela_codigo_transacao;

ALTER TABLE eventos.lancamento
ADD CONSTRAINT uk_movimento_pagbank_soberano
UNIQUE (codigo_transacao, tipo_evento, parcela);