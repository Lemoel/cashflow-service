ALTER TABLE eventos.lancamento
ALTER COLUMN tipo_evento TYPE VARCHAR(50);

ALTER TABLE eventos.lancamento
ALTER COLUMN meio_captura TYPE VARCHAR(50);

ALTER TABLE eventos.lancamento
ALTER COLUMN meio_pagamento TYPE VARCHAR(50);

COMMENT ON COLUMN eventos.lancamento.tipo_evento IS 'Tipo do evento - armazena nome completo do enum (ex: VENDA_OU_PAGAMENTO)';
COMMENT ON COLUMN eventos.lancamento.meio_captura IS 'Meio de captura - armazena nome completo do enum (ex: CHIP, TARJA)';
COMMENT ON COLUMN eventos.lancamento.meio_pagamento IS 'Meio de pagamento - armazena nome completo do enum (ex: PIX, CARTAO_CREDITO)';
