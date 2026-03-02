# Arquitetura Hexagonal - Cashflow Service

## Visão Geral

O **cashflow-service** é um microserviço em **Kotlin** e **Spring Boot 4** que segue a **Arquitetura Hexagonal (Ports and Adapters)**.

**Sistema apenas backend:** API REST pura. Não há UI neste repositório; frontends consomem as APIs.

**Fluxo padrão:** `API/Client → Controller → InputPort → Service → OutputPort ← Adapter → Repository (JDBC) → PostgreSQL`

## Princípios da Arquitetura Hexagonal

### 1. UseCase (core) não depende de nada externo

- Contém entidades, portas (InputPort / OutputPort) e serviços
- Não conhece Spring Data JDBC, Spring MVC, Controllers ou camada web
- Contém apenas regras de negócio e contratos (portas)

### 2. Adapters dependem do UseCase

- **Driven (persistence):** Adapters em `adapter/driven/persistence` implementam OutputPort e usam Repository Spring Data JDBC.
- **External (entrada HTTP):** Adapters em `adapter/external` expõem a API: **controller** (REST) e **dto** (request/response) ficam em `(fluxo)/adapter/external/controller` e `(fluxo)/adapter/external/dto`.
- O UseCase não conhece implementações concretas; controllers e DTOs pertencem ao módulo usecase, dentro do fluxo.

### 3. Fluxo de dependência: sempre para dentro (UseCase)

```
ExampleInputPort → ExampleService → ExampleOutputPort ← ExamplePersistenceAdapter → ExampleRepository → DB
(entrada)         (core)            (saída)              (adapter)                   (JDBC)
```

## Estrutura de Módulos

```
cashflow-service/
├── cashflow-service-commons   # Infraestrutura compartilhada (audit, exceções)
├── cashflow-service-database  # Migrations Flyway (SQL)
├── cashflow-service-app       # Bootstrap – Application, config, application.yml
└── cashflow-service-tests     # Testes de integração (Testcontainers)
```

### Dependências entre módulos

- **app** → database, commons  
- **tests** → app  

O domínio de negócio será implementado no módulo usecase (a ser criado quando necessário), contendo entidades, portas e serviços.

## Sequência das Camadas (fluxo de request)

### 1. UseCase – InputPort (porta de entrada)

- Interface que define os casos de uso (create, findById, findAll, findActive, update, delete)
- Implementada pelo **Service**

### 2. UseCase – Service (regras de negócio)

- Implementa `ExampleInputPort`
- Aplica regras de negócio
- Usa o **OutputPort** para persistência (nunca o Repository diretamente)
- Lança exceções de domínio (ex.: `ResourceNotFoundException`)

### 3. UseCase – OutputPort (porta de saída)

- Interface que define operações de persistência (save, findById, findAll, findActive, delete)
- Implementada pelo **Persistence Adapter**

### 4. UseCase – Persistence Adapter

- Implementa `ExampleOutputPort`
- Injeta e usa `ExampleRepository` (Spring Data JDBC)
- Traduz entre entidade de domínio e persistência

### 5. Repository (Spring Data JDBC) e Database

- `ExampleRepository` estende `BaseRepository<Example, UUID>`
- Flyway gerencia o schema; as migrations ficam em `cashflow-service-database`

## Estrutura de Pacotes (UseCase)

Cada fluxo segue o padrão: **entity**, **port**, **service**, **adapter/driven/persistence** e **adapter/external** (controller + dto).

### Convenções de pacotes

1. **Pacote da entidade:** Cada entidade deve ter seu próprio pacote `entity` dentro do fluxo.
2. **Pacote do fluxo:** Deve existir um pacote dedicado para cada fluxo de negócio.
3. **Underscore permitido:** Dentro de `br.com.cashflow.usecase`, é permitido criar pacotes com underscore (`_`).
4. **Estrutura do fluxo:** `(fluxo)/entity/`, `(fluxo)/port/`, `(fluxo)/service/`, `(fluxo)/adapter/driven/persistence/`, `(fluxo)/adapter/external/controller/`, `(fluxo)/adapter/external/dto/`.

```
br.com.cashflow.usecase/
├── commons/
│   └── adapter/driven/persistence/
│       └── BaseRepository.kt
├── example/
│   ├── entity/Example.kt
│   ├── port/
│   ├── service/
│   └── adapter/
│       ├── driven/persistence/
│       └── external/
│           ├── controller/
│           └── dto/
└── ...
```

- **Controller:** em `(fluxo)/adapter/external/controller` — usa apenas InputPort.
- **DTO:** em `(fluxo)/adapter/external/dto` — contratos de entrada/saída da API REST.

## Regras de Nomenclatura

| Elemento        | Padrão                         | Exemplo                      |
|----------------|---------------------------------|-----------------------------|
| Entidade       | Nome do domínio (sem sufixo)   | `Example`                   |
| Porta de entrada | `<Domínio>InputPort`         | `ExampleInputPort`          |
| Porta de saída | `<Domínio>OutputPort`          | `ExampleOutputPort`         |
| Serviço        | `<Domínio>Service`             | `ExampleService`            |
| Adapter        | `<Domínio>PersistenceAdapter`   | `ExamplePersistenceAdapter` |
| Repository     | `<Domínio>Repository`          | `ExampleRepository`         |

## Regras Obrigatórias

1. **Services** implementam **InputPort** e usam **OutputPort** para persistência. Nunca acessar Repository diretamente no service.
2. **Persistence Adapters** implementam **OutputPort** e usam **Repository** Spring Data JDBC.
3. **Controllers e DTOs** ficam no módulo **usecase**, em `(fluxo)/adapter/external/controller` e `(fluxo)/adapter/external/dto`. O módulo **app** contém apenas Application, config e recursos.
4. **UseCase** não depende de detalhes de infraestrutura (apenas de commons quando necessário).
5. Código de produção em **inglês** (classes, métodos, variáveis, tabelas, colunas). Documentação em `.md` pode ser em português.

---

**Última atualização:** 2026-03-01
