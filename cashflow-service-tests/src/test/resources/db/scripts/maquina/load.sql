SET search_path TO tenant_test;
INSERT INTO congregacao (id, tenant_id, nome, logradouro, bairro, numero, cidade, uf, cep, created_by_id, dti_created_date, last_modified_by_id, dti_last_modified_date)
VALUES (
  'b2c3d4e5-f6a7-8901-2345-678901bcdef0',
  '11111111-1111-1111-1111-111111111111',
  'Cong Maquina IT',
  '',
  '',
  '',
  '',
  '',
  '',
  'test',
  CURRENT_TIMESTAMP,
  'test',
  CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;
