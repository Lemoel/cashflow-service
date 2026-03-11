# Padrões de Backend (Kotlin/Spring)

## Stack

- Kotlin 2.2.0, Spring Boot 4.0.3, Spring Data JDBC, PostgreSQL, Flyway, MockK, JUnit 5.
- Em ambiente de desenvolvimento, o servidor roda na porta **8081** (configurável via `SERVER_PORT`).

## Estrutura de Pacotes (usecase)

1. **Pacote da entidade:** Cada entidade deve ter seu próprio pacote `entity` dentro do fluxo. Ex.: `user/entity/User.kt`, `age_range/entity/AgeRange.kt`.
2. **Pacote do fluxo:** Deve existir um pacote dedicado para cada fluxo de negócio. Ex.: fluxo `user`, fluxo `user_authentication`, fluxo `turma`.
3. **Underscore permitido:** Dentro de `br.com.cashflow.usecase`, é permitido criar pacotes com underscore (`_`). Ex.: `user_authentication`, `age_range`.
4. **Estrutura do fluxo:** `(fluxo)/entity/`, `(fluxo)/port/`, `(fluxo)/service/`, `(fluxo)/adapter/driven/persistence/`, `(fluxo)/adapter/external/controller/`, `(fluxo)/adapter/external/dto/`.

## Nomenclatura

- **Entidade**: `Example` (sem sufixo), classe (não data class) para compatibilidade com Spring Data JDBC.
- **InputPort**: `ExampleInputPort`; **OutputPort**: `ExampleOutputPort`.
- **Service**: `ExampleService` (implementa InputPort).
- **Repository**: `ExampleRepository` (estende `BaseRepository<Example, UUID>`).
- **PersistenceAdapter**: `ExamplePersistenceAdapter` (implementa OutputPort).
- **Controller**: `ExampleController` — fica em `usecase/(fluxo)/adapter/external/controller`.
- **DTO**: Request/Response — ficam em `usecase/(fluxo)/adapter/external/dto`.
- **Exceções**: `BusinessException`, `ResourceNotFoundException` (commons).

## Convenções de Código

- **Sem comentários:** Não adicionar comentários ao código (ver `.cursor/rules/05-sem-comentarios-no-codigo.md`).
- Código em **inglês**; mensagens de UI em **português** (ver `.cursor/rules/07-idiomas.md`).
- Preferir construtor com injeção de dependências (ex.: `class ExampleService(private val outputPort: ExampleOutputPort)`).
- Serviços: usar `@Service` e `@Transactional` em métodos de escrita.
- Adapters: usar `@Component`.
- Repositories: interface com `@NoRepositoryBean` em `BaseRepository`; preferir métodos derivados (ver seção Spring Data JDBC).

## Entidade Spring Data JDBC

- **Todas as entidades** que representam dados persistidos devem herdar de `Auditable` (obrigatório). Import: `br.com.cashflow.commons.audit.Auditable`. Exceção: entidades que mapeiam tabelas legadas sem colunas de auditoria (created_by, created_date, last_modified_by, last_modified_date) não precisam estender Auditable — exemplo: `Acesso` (tabela `eventos.acesso` com `data`, `mod_date_time`).
- Entidades que estendem `Auditable` devem ser `class` (não data class) — mutabilidade necessária para auditoria.
- Usar `@Table("example")`, `@Id`, `@Column("coluna")` de `org.springframework.data.relational.core.mapping`.
- UUID: implementar `BeforeConvertCallback<T>` para gerar `UUID.randomUUID()` quando `id == null`; registrar como `@Component`.
- Auditoria: `@EnableJdbcAuditing` na aplicação; `@CreatedDate`, `@LastModifiedDate` (e opcionalmente `@CreatedBy`, `@LastModifiedBy`) em `Auditable`.
- Schema: criar tabelas via Flyway; JDBC não gera DDL.

## Spring Data JDBC: prioridade e métodos derivados

- **Prioridade de acesso a dados:** Qualquer busca ou operação em banco de dados deve ser criada **primordialmente** usando Spring Data JDBC (interface estendendo `CrudRepository`, métodos derivados ou `@Query`). Só em **último caso**, quando não for possível atender aos critérios de busca com Spring Data JDBC (consultas complexas, joins, agregações ou SQL nativo indispensável), pode-se usar `JdbcTemplate` em uma implementação customizada (ex.: `ExampleRepositoryImpl`).
- Preferir sempre que possível os métodos derivados (nomes de método transformados em SQL automaticamente).
- Preferir nomes como `findByClassIdOrderByStartDateDesc`, `findFirstByTeacherIdAndClassId` em vez de `@Query` explícita.
- Usar `@Query` com SQL explícita apenas quando o nome do método derivado ficar muito longo ou a lógica for complexa (ex.: overlap de datas, condições OR compostas).
- Exemplos: `findByX`, `findByXAndY`, `findFirstByX`, `findByXOrderByYDesc`, `existsByX`.

## Controllers

- Controller apenas recebe Request e retorna Response.
- Toda regra de negócio deve estar no domínio (não no controller nem no adapter).
- Controller apenas chama InputPort (não acessa serviço nem repositório diretamente).

## Mapeamento de objetos

- Usar extension functions para mapeamento. Nomear: `toResponse()`, `toDomain()`, `toEntity()`.
- Exemplo: `fun User.toResponse(): UserResponse`; `fun CreateUserRequest.toDomain(): User`.
- Proibido: mapear objetos dentro de controllers (lógica nas extension functions; controller pode chamar `entity.toResponse()`); colocar lógica de conversão dentro do domínio; usar reflexão ou bibliotecas automáticas de mapeamento sem solicitação explícita.

## Estilo Kotlin e princípios

- Usar data class quando fizer sentido; para entidades persistidas que estendem Auditable, usar sempre class.
- Preferir funções de extensão para conversões.
- Evitar nullable desnecessário. Evitar `var` quando puder ser `val`.
- Usar nomes claros e sem abreviações.
- O projeto deve ser: simples, testável, independente de framework (domínio sem anotações de framework), fácil de evoluir, fácil de entender em menos de 5 minutos. Em caso de dúvida, optar pela solução mais simples e previsível.
- Proibido: misturar camadas; colocar regra de negócio em adapter; usar `@Entity` ou anotações de persistência no domínio; criar código mágico; criar dependências desnecessárias.

## Testes

- **Unitários** (usecase): MockK para OutputPort, JUnit 5, AssertJ; sem contexto Spring.
- **Integração** (tests): herdar `PostgresqlBaseTest`, Testcontainers PostgreSQL, `@SpringBootTest`, injetar InputPort ou Service. Regras detalhadas em `.cursor/rules/08-testes-integracao.md`.
- **Persistence Adapters e DTOs:** testes unitários obrigatórios conforme `.cursor/rules/09-testes-adapter-dto.md`.
- Nomes de testes em inglês; em Kotlin pode usar backticks: `` `findById should return example when found`() ``.

## Qualidade

- ktlint: rodar `ktlintCheck` / `ktlintFormat` antes de commit.
- JaCoCo: cobertura mínima configurada no build (ex.: 60%); ajustar conforme necessidade do template.
