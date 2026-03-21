SET search_path TO tenant_test;
INSERT INTO maquina (
    id,
    numero_serie_leitor,
    banco_id,
    ativo,
    version,
    created_by_id,
    dti_created_date,
    last_modified_by_id,
    dti_last_modified_date
) VALUES (
    '11111111-2222-3333-4444-555555555501',
    'SERIE_IT_BULK',
    'a1b2c3d4-e5f6-7890-1234-567890abcdef',
    TRUE,
    0,
    'test',
    CURRENT_TIMESTAMP,
    'test',
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;
