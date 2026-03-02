---
name: kotlin-best-practices
description: Guides Kotlin code following software engineering best practices and Kotlin idioms. Use when writing or refactoring Kotlin code, implementing services/entities/ports, or when the user asks about Kotlin style, null safety, scope functions, testing with MockK, or clean architecture in Kotlin.
---

# Kotlin Best Practices

## Principles

- Prefer **immutability**: `val` over `var`, read-only collections (`List`, `Map`, `Set`).
- Use **null safety** explicitly: nullable types (`T?`), `requireNotNull`/`checkNotNull` for preconditions, `?.`/`?:`/`!!` only when justified.
- Keep **single responsibility**: small, focused functions and classes; extract when a block does more than one thing.
- Align with **project conventions**: see `.cursor/rules/02-padroes-backend.md` and `ARCHITECTURE.md` for ports, adapters, naming, and Spring Data JDBC.

### Estrutura de pacotes (backend Kotlin)

Ao criar ou modificar código backend no usecase:

1. **Pacote da entidade:** Cada entidade deve ter seu próprio pacote `entity` dentro do fluxo. Ex.: `user/entity/User.kt`, `turma/entity/Turma.kt`.
2. **Pacote do fluxo:** Deve existir um pacote dedicado para cada fluxo de negócio. Ex.: fluxo `user`, fluxo `user_authentication`, fluxo `turma`.
3. **Underscore permitido:** Dentro de `br.com.bibleschool.usecase`, é permitido criar pacotes com underscore (`_`). Ex.: `user_authentication`, `age_range`.
4. **Estrutura do fluxo:** `(fluxo)/entity/`, `(fluxo)/port/`, `(fluxo)/service/`, `(fluxo)/adapter/driven/persistence/`, `(fluxo)/adapter/external/controller/`, `(fluxo)/adapter/external/dto/`.

---

## Null Safety

- Prefer non-null types; use `T?` only when the value can be absent.
- Use `requireNotNull(x) { "message" }` or `checkNotNull(x)` for preconditions; avoid `!!` in production code.
- Prefer `?.let { }` or `?.also { }` over `if (x != null) { }` when transforming or side-effecting.
- Use `Elvis` for defaults: `value ?: default`; for early return: `return value ?: return` or `value ?: run { log.warn("..."); return }`.

```kotlin
// Prefer
fun process(id: UUID): Entity = outputPort.findById(id)
    ?: throw ResourceNotFoundException("Entity not found: $id")

// Avoid
fun process(id: UUID): Entity = outputPort.findById(id)!!
```

---

## Immutability and Data Structures

- **`val` by default**; use `var` only when the reference must change (e.g. entity fields mapped by Spring Data JDBC).
- Prefer `listOf()`, `mapOf()`, `setOf()` (read-only); use `mutableListOf()` only when building or mutating is required.
- Return read-only types from public APIs: `List<T>`, not `MutableList<T>`.

```kotlin
// Public API
fun findAll(): List<Example> = outputPort.findAll()

// Building a list
val ids = items.map { it.id }.toList()
```

---

## Classes and Types

### class vs data class

- **`class` (classe comum)**: controle total; não gera automaticamente `equals()`, `hashCode()`, `toString()` nem `copy()`. Use quando há mutabilidade ou herança (ex.: base de auditoria).
- **`data class`**: o compilador gera automaticamente `equals()`, `hashCode()`, `toString()` e `copy()` com base nas propriedades do construtor primário. O construtor primário deve ter pelo menos um parâmetro. Use para holders de dados (DTOs, request/response, valores imutáveis).

### Entidades com auditoria (Auditable)

- Entidades que precisam de auditoria devem herdar de `Auditable` (ou da base de auditoria do projeto).
- Essas entidades devem ser **`class`**, não **`data class`**: exigem mutabilidade (campos alterados em update), não usam `copy()`; o fluxo é alterar a mesma instância e persistir.
- A base `Auditable` expõe campos como `createdDate`, `lastModifiedDate` (ou equivalentes), preenchidos pelo framework.

### Onde usar cada um

| Tipo | Use | Exemplo |
|------|-----|---------|
| Entidade persistida com auditoria | `class` estendendo `Auditable` | `class User(...) : Auditable()` |
| DTO / request / response | `data class` | `data class UserResponse(...)` |
| Entidade sem auditoria e imutável | `data class` | `data class IdName(val id: UUID, val name: String)` |

### Sealed classes/interfaces

- Use para hierarquias fechadas (result types, estados, comandos) para permitir `when` exaustivo.

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val value: T) : Result<T>
    data class Error(val message: String) : Result<Nothing>
}
```

### Objects

- Use para singletons (ex.: constantes, factories); prefira injeção de dependência para serviços.

### Resumo

- **Entidades com auditoria**: `class` + `Auditable`; **DTOs**: `data class`.

---

## Scope Functions

Choose by intent and return value:

| Function | Receiver as `this` | Return value   | Typical use                    |
|----------|--------------------|----------------|--------------------------------|
| `let`    | `it`               | lambda result  | null-safe transform, narrow scope |
| `also`   | `it`               | receiver       | side effects, logging          |
| `apply`  | `this`             | receiver       | configuring object              |
| `run`    | `this`             | lambda result  | transform + multiple statements |
| `with`   | `this` (arg)       | lambda result  | group calls on one object      |

- Prefer `let` for null-safe chains: `nullable?.let { doSomething(it) }`.
- Prefer `apply` for builder-style setup; avoid nesting many scope functions in one expression.

---

## Extension Functions

- Use for domain or API clarity without modifying the type: `fun String.toSlug(): String`.
- Prefer file-level private extensions when used in a single file; place shared extensions in a dedicated file (e.g. `StringExtensions.kt`).
- Avoid extending types you don’t own with ambiguous or overly broad behavior.

---

## Functions and Expressions

- Prefer **expression bodies** for one-liners: `override fun findById(id: UUID): Example? = outputPort.findById(id)`.
- Use **meaningful names**; for tests, backticks are allowed: `` `findById returns entity when found`() ``.
- Prefer **default parameters** over overloads when the same behavior applies.
- Keep **parameters** to a few; use a data class or config object for many options.

---

## Dependency Injection and Construction

- Prefer **constructor injection**: `class ExampleService(private val outputPort: ExampleOutputPort) : ExampleInputPort`.
- Use `private val` for dependencies; avoid lateinit or optional fields for required collaborators.

---

## Exceptions and Errors

- Use **domain exceptions** (e.g. `ResourceNotFoundException`, `BusinessException`) in the use case layer; map at boundaries (e.g. controller/advice) to HTTP or DTOs.
- Prefer **fail-fast**: validate inputs and throw early with clear messages; avoid silent defaults for invalid data.

---

## Testing (JUnit 5 + MockK)

- **Unit tests**: MockK for ports (`every`, `verify`); JUnit 5; AssertJ for fluent assertions. No Spring context.
- **Integration tests**: `PostgresqlBaseTest`, Testcontainers, `@SqlSetUp`/`@SqlTearDown`, scripts em `db/scripts/<contexto>/`. Regras completas em `.cursor/rules/08-testes-integracao.md`.
- **Persistence Adapters:** testes unitários obrigatórios com MockK; mock do Repository; validar delegação e retorno. Regras completas em `.cursor/rules/09-testes-adapter-dto.md`.
- **DTOs:** testes obrigatórios para DTOs com lógica; para DTOs puros, testar extension functions de mapeamento (`toResponse`, etc.). Regras completas em `.cursor/rules/09-testes-adapter-dto.md`.
- **Naming**: describe behavior: `` `findById throws when not found`() ``.
- Use `every { port.method(...) } returns value` and `verify { port.method(...) }`; prefer `slot`/`coEvery` when needed for coroutines or capturing args.
- Prefer **one logical assertion per test** when it improves readability; group related checks in a single test when they form one scenario.

```kotlin
@Test
fun `findById returns entity when found`() {
    every { outputPort.findById(id) } returns entity
    assertThat(service.findById(id)).isEqualTo(entity)
    verify(exactly = 1) { outputPort.findById(id) }
}
```

---

## Coroutines (when used)

- Prefer `suspend` at the boundary (e.g. controller or use case); keep core logic in plain or suspend functions as appropriate.
- Use `runTest` in tests for suspend code; avoid blocking in coroutine-based code.
- Prefer structured concurrency: scope and job hierarchy; avoid global `GlobalScope.launch` in application code.

---

## Anti-Patterns to Avoid

- **`!!`**: replace with null-safe handling or explicit checks.
- **Mutable collections in public API**: return `List`/`Map`/`Set`, not mutable subtypes.
- **Large data classes with many mutable vars**: split or use builder/immutable pattern where possible.
- **Scope function nesting**: e.g. `x?.let { it.also { ... }.run { ... } }`; flatten or use a local variable.
- **Business logic in controllers or adapters**: keep in services (use case); controllers and adapters orchestrate and translate.
- **Ignoring Kotlin conventions**: use ktlint (`ktlintCheck`/`ktlintFormat`) and fix reported issues.

---

## Quick Reference

- **Entidades com auditoria**: `class` + `Auditable`; **DTOs**: `data class`.
- **Project rules**: `.cursor/rules/02-padroes-backend.md`, `ARCHITECTURE.md`, `AGENTS.md`.
- **Format**: `./gradlew ktlintFormat` or `make format`.
- **Tests**: unit in usecase module (MockK + JUnit 5); integration in tests module (Testcontainers + Spring); regras em `.cursor/rules/08-testes-integracao.md`.
