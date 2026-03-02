# Arquitetura Hexagonal - Cashflow Service

## Visão Geral

O projeto segue **Arquitetura Hexagonal (Ports and Adapters)**. O fluxo de dependência é sempre para dentro: controllers e adapters dependem do núcleo (usecase); o núcleo não conhece detalhes de infraestrutura.

## Princípios

### 1. Núcleo (usecase) não depende de nada externo
- Contém entidades, ports (interfaces), serviços e adapters de persistência que implementam os ports.
- O usecase conhece Spring Data JDBC apenas nos adapters e repositórios; os serviços dependem apenas das interfaces (ports).

### 2. Porta de entrada (InputPort)
- Interface usada pelos controllers.
- Implementada pelo **Service** (ex.: `ExampleService` implementa `ExampleInputPort`).

### 3. Porta de saída (OutputPort)
- Interface usada pelo Service para persistência.
- Implementada pelo **PersistenceAdapter** (ex.: `ExamplePersistenceAdapter` implementa `ExampleOutputPort`).

### 4. Fluxo de dependência

```
Controller → InputPort (interface) ← Service → OutputPort (interface) ← PersistenceAdapter → Repository → DB
```

## Sequência de Implementação

1. **Entity** – entidade com mapeamento Spring Data JDBC no usecase (ex.: `Example`).
2. **OutputPort** – interface de persistência (save, findById, findAll, findActive, delete).
3. **InputPort** – interface de casos de uso (create, findById, findAll, findActive, update, delete).
4. **Repository** – interface Spring Data JDBC (estende `BaseRepository`).
5. **PersistenceAdapter** – implementa OutputPort e usa o Repository.
6. **Service** – implementa InputPort, injeta OutputPort e contém regras de negócio.
7. **Controller** – em `(fluxo)/adapter/external/controller`; injeta apenas InputPort e chama seus métodos.
8. **DTO** – em `(fluxo)/adapter/external/dto`; request/response da API REST.

## Regras Críticas

- **Controllers e DTOs** ficam no **usecase**, em `(fluxo)/adapter/external/controller` e `(fluxo)/adapter/external/dto`; o módulo **app** não contém controllers.
- Controller **nunca** injeta OutputPort nem Repository; apenas InputPort.
- Service **nunca** acessa Repository diretamente; apenas OutputPort.
- Entidades (mapeamento Spring Data JDBC) ficam no módulo **usecase** (não em módulo separado de persistência).
- Exceções de negócio (`ResourceNotFoundException`, `BusinessException`) vêm do **commons** e são tratadas no **ExceptionAdvice**.

## Estrutura de Pacotes (usecase)

Cada fluxo (ex.: `example`, `user`, `user_authentication`, `age_range`, `turma`) contém entity, port, service e **adapter** com dois tipos: **driven** (persistência) e **external** (entrada HTTP: controller + dto).

### Convenções de pacotes

1. **Pacote da entidade:** Cada entidade deve ter seu próprio pacote `entity` dentro do fluxo. Ex.: `user/entity/User.kt`, `age_range/entity/AgeRange.kt`, `turma/entity/Turma.kt`.
2. **Pacote do fluxo:** Deve existir um pacote dedicado para cada fluxo de negócio. Ex.: fluxo `user` (entidade User), fluxo `user_authentication` (autenticação), fluxo `turma` (entidade Turma).
3. **Underscore permitido:** Dentro de `br.com.cashflow.usecase`, é permitido criar pacotes com underscore (`_`). Ex.: `user_authentication`, `age_range`.
4. **Estrutura do fluxo:** `(fluxo)/entity/`, `(fluxo)/port/`, `(fluxo)/service/`, `(fluxo)/adapter/driven/persistence/`, `(fluxo)/adapter/external/controller/`, `(fluxo)/adapter/external/dto/`.

```
usecase/
├── commons/adapter/driven/persistence/
│   └── BaseRepository.kt
├── example/
│   ├── entity/Example.kt
│   ├── port/
│   ├── service/
│   └── adapter/
│       ├── driven/persistence/
│       └── external/
│           ├── controller/
│           └── dto/
├── user/
│   ├── entity/User.kt
│   └── ...
├── user_authentication/
│   ├── service/AuthService.kt
│   └── ...
└── turma/
    ├── entity/Turma.kt
    └── ...
```

- **Controller:** `(fluxo)/adapter/external/controller` — usa apenas InputPort.
- **DTO:** `(fluxo)/adapter/external/dto` — contratos de entrada/saída da API REST.

## Regra de Separação: Núcleo de Entidade vs Fluxo

O projeto DEVE separar rigidamente o núcleo estrutural da entidade dos fluxos (use cases). Essa separação é obrigatória.

### 1. Pacote de entidade (core)

Exemplo: `school_class`

Representa o NÚCLEO estrutural da entidade.

**DEVE conter apenas:**
- entity/
- port/ (apenas portas de saída — OutputPort)
- adapter/driven/
- repository
- persistence adapter

**NÃO PODE conter:**
- controller
- DTO
- request
- response
- input port
- service de fluxo
- regra de orquestração
- lógica específica de caso de uso

É estrutural e reutilizável. NÃO conhece fluxos.

### 2. Pacote de fluxo (feature)

Exemplo: `school_class_management`

Representa um FLUXO específico da aplicação.

**DEVE conter:**
- port/ (input port)
- service/ (use case)
- adapter/external/controller
- dto/

**NÃO PODE conter:**
- entidade
- implementação de repositório
- adapter de persistência
- lógica estrutural da entidade

DEPENDE do núcleo da entidade.

### 3. Regra de dependência

- Fluxo → depende do núcleo da entidade
- Núcleo da entidade → NÃO depende de fluxo

**É proibido:**
- Entidade depender de fluxo
- Persistência depender de fluxo
- OutputPort conhecer InputPort
- Dependência circular

### 4. Responsabilidade

**Núcleo da entidade:**
- Modela o conceito
- Define contratos de saída
- Define persistência
- Contém regra de negócio pura

**Fluxo:**
- Orquestra caso de uso
- Define entrada
- Define saída
- Conecta controller ao domínio

### 5. Regra de escalabilidade

Para cada novo fluxo: criar um NOVO pacote de fluxo.

Exemplo:
- school_class_management
- school_class_enrollment
- school_class_report

Todos reutilizando o núcleo `school_class`.

### 6. Proibição absoluta

A IA NÃO PODE:
- Misturar fluxo com núcleo
- Criar entidade dentro do fluxo
- Criar repositório dentro do fluxo
- Criar DTO dentro do núcleo
- Criar dependência circular

Se houver dúvida: separar.

### 7. Princípio estrutural

- Entidade é permanente. Fluxo é transitório.
- Estrutura é estável. Caso de uso é variável.
- A arquitetura deve refletir isso.

## Web (adaptador de entrada)

Controllers REST são o adaptador de entrada HTTP e ficam no **usecase**, em `(fluxo)/adapter/external/controller`. Recebem requisições, chamam o InputPort e retornam JSON (APIs REST). O módulo **app** contém apenas Application, config e recursos. Este serviço é apenas backend; não há views nem Thymeleaf.
