# Migrations Flyway

## Regras obrigatórias

### Comentários

- É proibido usar comentários em arquivos de migration (incluindo `--` e `/* */`).

### COMMENT ON

- É proibido usar:
  - `COMMENT ON TABLE`
  - `COMMENT ON COLUMN`
  - `COMMENT ON CONSTRAINT`

### CASCADE em relacionamentos

- Em nenhuma migration com relacionamento (foreign key) deve ser usado `ON DELETE CASCADE` nem `ON UPDATE CASCADE`.
- Definir FKs sem cláusulas CASCADE.

### Nomenclatura

- O nome do arquivo deve seguir o algoritmo dos scripts oficiais do projeto.
- Scripts: `cashflow-service-database/create_migration_linux.sh` e `cashflow-service-database/create_migration_mac.sh`.
- Formato: `V${YYYYMMDDHHMMSS}__${nome_descritivo}.sql`
- Exemplo: `V20260227143000__add_professor_table.sql`

### Colunas de status / enum

- Não usar tipo `CREATE TYPE ... AS ENUM` do PostgreSQL para colunas que serão mapeadas por Spring Data JDBC com enums Kotlin (String).
- O driver JDBC envia os valores como `character varying`; colunas do tipo ENUM nativo geram erro "column is of type X but expression is of type character varying".
- Usar `VARCHAR(n)` (ex.: 20) com constraint `CHECK (coluna IN ('VAL1', 'VAL2', ...))` para garantir os valores permitidos e compatibilidade com o mapeamento padrão.
