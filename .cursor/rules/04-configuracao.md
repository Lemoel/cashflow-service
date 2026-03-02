# Configuração - Cashflow Service

## Arquivos de Configuração (app)

- **application.yml** – nome da aplicação, profile ativo (`SPRING_PROFILES_ACTIVE`, default `dev`), server port (default 8081), actuator (health, info).
- **application-dev.yml** – datasource (DB_URL, DB_USER, DB_PASSWORD), Flyway habilitado, logging DEBUG.
- **application-prod.yml** – datasource via variáveis de ambiente, logging INFO.
- **logback-spring.xml** – formato de log por profile (dev: legível; prod: JSON estruturado, se desejado).

*Nota: Este serviço é apenas backend; não utiliza Thymeleaf nem outras tecnologias de UI.*

## Variáveis de Ambiente e .env

- Usar **springboot4-dotenv** para carregar `.env` na raiz.
- `.env` não versionado; `.env.example` versionado sem valores sensíveis.
- Exemplo: `DB_USER`, `DB_PASSWORD`, `DB_URL`, `DB_NAME`.

## Banco de Dados

- PostgreSQL; criar banco (ex.: `CREATE DATABASE eventosdb;`) antes de rodar a aplicação.
- Flyway: migrations em `database/src/main/resources/db/migration/` (V001, V002, V003...).
- Spring Data JDBC não gera DDL; Flyway é a fonte da verdade do schema.

## Perfis

- **dev** – default; datasource com fallback para valores locais; SQL e logs em DEBUG.
- **prod** – datasource obrigatório via env.

## Auditoria

- `AuditingConfig` no app: `@EnableJdbcAuditing` e bean `AuditorAware<String>` (ex.: `AuditorAwareImpl` retornando `"system"`).
- Entidades que estendem `Auditable` preenchem automaticamente createdBy, createdDate, lastModifiedBy, lastModifiedDate.

## Actuator

- Expor apenas `health` e `info`; `show-details: when-authorized` para health.
- Porta padrão em desenvolvimento: **8081** (configurável via `SERVER_PORT`).

## Docker

- Dockerfile multi-stage: build com JDK, execução com JRE; usuário não-root; HEALTHCHECK com actuator health.
- Build: `./gradlew :cashflow-service-app:jibDockerBuild` ou `make docker-build`; variáveis de ambiente via `--env-file .env` ou equivalente.
