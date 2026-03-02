# Testes de Integração

## Localização e estrutura

- **Módulo:** `cashflow-service-tests`
- **Pacote:** `br.com.cashflow.tests.usecase.<fluxo>.service`
- **Sufixo:** `IT` (ex.: `ExampleServiceIT`)

## Classe base

- Herdar `PostgresqlBaseTest` (pacote `br.com.cashflow.tests.base.postgresql`)
- Não usar `AbstractBaseTest` (não existe no projeto)

## Infraestrutura

- **Testcontainers:** PostgreSQL via `CashflowDataSource.POSTGRES_CONTAINER`
- **Spring Boot Test:** `@SpringBootTest(classes = [CashflowApplication::class])`
- **JdbcTemplate:** disponível via herança (`PostgresqlBaseTest` → `CashflowDataSource` → `AbstractDatasourceBaseTest`)

## Dados de teste: @SqlSetUp e @SqlTearDown

- **@SqlSetUp:** executa antes de cada método de teste (`Sql.ExecutionPhase.BEFORE_TEST_METHOD`)
- **@SqlTearDown:** executa após cada método de teste (`Sql.ExecutionPhase.AFTER_TEST_METHOD`)
- **Caminho dos scripts:** `src/test/resources/db/scripts/<contexto>/load.sql` e `teardown.sql`
- Exemplo: `@SqlSetUp(value = ["/db/scripts/example/load.sql"])`, `@SqlTearDown(value = ["/db/scripts/example/teardown.sql"])`

## Quando usar scripts vs dados de migration

- **Scripts SQL:** dados específicos do teste; isolamento por contexto
- **Migrations Flyway:** estrutura do banco (tabelas, FKs); executadas automaticamente pelo Testcontainers

## Injeção

- Injetar **InputPort** ou **Service** via `@Autowired`
- Preferir InputPort para manter consistência com a arquitetura hexagonal

## Nomenclatura

- Código e nomes de testes em **inglês**
- Em Kotlin, pode usar backticks: `` `findById should return entity when found`() ``

## Criação de dados

- **Via InputPort:** para entidades do domínio
- **Via jdbcTemplate:** para dados auxiliares ou quando o InputPort não expõe o método necessário

## Ordem do teardown

- Respeitar dependências de FK: tabelas dependentes antes das referenciadas

```kotlin
@SqlTearDown(value = ["/db/scripts/dependent/teardown.sql", "/db/scripts/referenced/teardown.sql"])
```

## Sem comentários

- Não adicionar comentários ao código (regra do projeto; ver `.cursor/rules/05-sem-comentarios-no-codigo.md`)

## Assertions

- Usar **AssertJ:** `assertThat(...)`, `assertThatThrownBy { ... }`

## Cenários típicos

- **Happy path:** CRUD completo, listagens, associações
- **Exceções esperadas:** `assertThatThrownBy { ... }.isInstanceOf(BusinessException::class.java).hasMessageContaining("...")`

## Referências

- `PostgresqlBaseTest`: `br.com.cashflow.tests.base.postgresql.PostgresqlBaseTest`
- `SqlSetUp`, `SqlTearDown`: `br.com.cashflow.tests.base.postgresql.annotations`
- Testes unitários de Adapters e DTOs: `.cursor/rules/09-testes-adapter-dto.md`
