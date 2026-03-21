SET search_path TO tenant_test;
DELETE FROM lancamento WHERE codigo_transacao LIKE 'IT-BULK-%';
DELETE FROM maquina WHERE id = '11111111-2222-3333-4444-555555555501';
