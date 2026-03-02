# Contexto do Projeto - Cashflow Service (Kotlin)

## Visão Geral

O **cashflow-service** é um microserviço em **Kotlin** com **Spring Boot 4** e **arquitetura hexagonal (Ports & Adapters)**. Possui 4 módulos Gradle.

**Sistema apenas backend:** API REST pura. Não há UI neste repositório; frontends consomem as APIs. Endpoints REST/RESTful até nível 3 (Richardson Maturity Model). Persistência via Spring Data JDBC. Em ambiente de desenvolvimento, o servidor roda na porta **8081** (configurável via `SERVER_PORT`).

### Objetivo

Microserviço para fluxo de caixa. Todo o código (Kotlin, SQL, configurações) deve ser escrito em **inglês**. Apenas documentação em `.md` pode ser em português.

## Stack Tecnológica

### Backend
- **Kotlin** 2.2.0
- **Spring Boot** 4.0.3
- **Spring Framework** 7.x
- **Spring Data JDBC** – persistência (todo acesso a dados)
- **PostgreSQL** – banco de dados
- **Flyway** – migrações
- **Jakarta EE 11** (Servlet 6.1)
- **Java 21**
- **Gradle** (Kotlin DSL, multi-módulo)
- **springboot4-dotenv** 5.1.0 – leitura de `.env`

### Qualidade e Testes
- **ktlint** (plugin Gradle 14.0.1) – lint e formatação
- **JaCoCo** – cobertura de testes
- **MockK** 1.14.3 – mocking em testes unitários
- **Testcontainers** – testes de integração com PostgreSQL
- **JUnit 5** – framework de testes

## Estrutura de Módulos

```
cashflow-service/
├── cashflow-service-commons/   # Infraestrutura compartilhada (audit, exceções, ErrorResponse)
├── cashflow-service-database/  # Migrations Flyway (apenas recursos SQL)
├── cashflow-service-app/       # Bootstrap: Application, config, application.yml (sem controllers)
└── cashflow-service-tests/     # Testes de integração (Testcontainers)
```

### Dependências entre módulos

- **app** → database, commons
- **tests** → app

## Fluxo de Dados (exemplo)

```
Client/API → Controller REST → ExampleInputPort → ExampleService
    → ExampleOutputPort ← ExamplePersistenceAdapter → ExampleRepository → PostgreSQL
```

## Comandos Principais

- `./gradlew :cashflow-service-app:bootRun` ou `make run` – subir a aplicação
- `./gradlew clean build` ou `make build` – compilar
- `./gradlew ktlintCheck` – verificar estilo
- `./gradlew ktlintFormat` – formatar código
- `./gradlew test` – todos os testes
- `./gradlew :cashflow-service-tests:test` – testes de integração (requer Docker)
- `./gradlew jacocoTestReport` – relatório de cobertura

## Regra de Idioma

Todo código (nomes de classes, métodos, variáveis, tabelas, colunas, rotas, mensagens de log, nomes de testes) deve estar em **inglês**. Exceção: arquivos de documentação em `.md` (como este) podem estar em português.
