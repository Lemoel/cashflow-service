# Instruções para Agentes de IA - Cashflow Service

Este documento fornece diretrizes para IAs que trabalham no **cashflow-service**: microserviço em Kotlin + Spring Boot 4 com arquitetura hexagonal.

**Sistema apenas backend:** API REST pura. Endpoints REST/RESTful nível 3. Persistência via Spring Data JDBC. Em desenvolvimento, servidor na porta **8081**.

## Regras do Projeto

As regras detalhadas ficam em `.cursor/rules/` (quando existirem). Os arquivos abaixo são referência rápida.

- **ARCHITECTURE.md**: Arquitetura hexagonal, módulos, portas (InputPort/OutputPort), fluxo Controller REST → InputPort → Service → OutputPort ← Adapter → Repository. Controllers e DTOs ficam no usecase em `(fluxo)/adapter/external/controller` e `(fluxo)/adapter/external/dto`.

**Use ARCHITECTURE.md para orientação sobre camadas e dependências.**

## Stack Tecnológica

### Backend

- **Linguagem:** Kotlin 2.2.0 
- **Framework:** Spring Boot 4.0.3 
- **Persistência:** Spring Data JDBC 
- **Banco:** PostgreSQL 
- **Migrations:** Flyway (em `cashflow-service-database`). Nomenclatura: `V${YYYYMMDDHHMMSS}__${nome_descritivo}.sql` (ver `cashflow-service-database/create_migration_mac.sh`). 
- **Build:** Gradle (Kotlin DSL), multi-módulo 

### Qualidade e Testes

- **Lint/formatação:** ktlint (plugin Gradle) 
- **Testes unitários:** JUnit 5 + MockK (módulo usecase) 
- **Testes de integração:** Spring Boot Test + Testcontainers (módulo tests) 
- **Cobertura:** JaCoCo (threshold configurável, ex.: 60%) 

## Referência Rápida

### Estrutura de Módulos

```
cashflow-service-commons   → audit, exceções, ErrorResponse, ExceptionAdvice
cashflow-service-database  → migrations Flyway (SQL)
cashflow-service-usecase   → CORE: entidades, InputPort, OutputPort, Service, Adapter (driven + external), Repository; controllers e DTOs em (fluxo)/adapter/external/
cashflow-service-app       → Application.kt, config, application.yml, perfis dev/prod (sem controllers)
cashflow-service-tests     → testes de integração (PostgresqlBaseTest); regras em `.cursor/rules/08-testes-integracao.md`
```

### Princípios da Arquitetura Hexagonal

- **Service** → implementa **InputPort**, usa **OutputPort** para persistência. 
- **Persistence Adapter** → implementa **OutputPort**, usa **Repository** (Spring Data JDBC). 
- **UseCase** não depende de web nem de detalhes de infraestrutura. 

### Convenções de Nomenclatura (código em inglês)

- **Portas:** `ExampleInputPort`, `ExampleOutputPort` 
- **Serviço:** `ExampleService` 
- **Adapter:** `ExamplePersistenceAdapter` 
- **Repository:** `ExampleRepository` 
- **Entidade:** `Example` (sem sufixo; mapeamento Spring Data JDBC no usecase) 

### Comandos Principais

```bash
make run          # Subir aplicação
make build        # Compilar
make test         # Todos os testes
make quality      # ktlint + testes + cobertura
make format       # Formatar com ktlint
make coverage     # Relatório JaCoCo
make docker-build # Build da imagem Docker
make setup-hooks  # Hook pre-commit com ktlint
```

Ou via Gradle:

```bash
./gradlew :cashflow-service-app:bootRun
./gradlew clean build
./gradlew test
./gradlew qualityCheck
./gradlew ktlintFormat
```

### Testes

- **Unitários:** `./gradlew :cashflow-service-usecase:test` (MockK, sem Spring)
- **Integração:** `./gradlew :cashflow-service-tests:test` (Testcontainers PostgreSQL) 

### Idioma

- **Código:** inglês (classes, métodos, variáveis, tabelas, colunas, rotas, mensagens de log, nomes de testes). 
- **Documentação .md:** pode ser em português. 

## Pontos de Atenção

1. **Sem comentários no código:** Não adicionar comentários ao código-fonte (backend e frontend). Ver `.cursor/rules/05-sem-comentarios-no-codigo.md`.
2. **Migrations Flyway:** Sem comentários, sem COMMENT ON, sem CASCADE em FKs; nomenclatura conforme scripts. Ver `.cursor/rules/06-migrations-flyway.md`.
3. Respeitar o fluxo **InputPort → Service → OutputPort ← Adapter → Repository**.
4. Não acessar Repository diretamente em Services.
5. Validar com `make build` e `make test` (ou `./gradlew qualityCheck`) antes de commit.
6. Usar Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`).
7. **Testes de integração:** Ver `.cursor/rules/08-testes-integracao.md` para regras completas (PostgresqlBaseTest, SqlSetUp/SqlTearDown, scripts).
8. **Testes de Adapters e DTOs:** Ver `.cursor/rules/09-testes-adapter-dto.md` para regras obrigatórias de testes unitários de Persistence Adapters e DTOs.

---

**Última atualização:** 2026-03-01
