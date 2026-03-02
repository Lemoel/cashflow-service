# Testes de Persistence Adapters e DTOs

## Obrigatoriedade

Todo `*PersistenceAdapter` e todo pacote `adapter.external.dto` deve ter cobertura de testes.

## Persistence Adapters (adapter.driven.persistence)

- **Testes unitários obrigatórios** no módulo `usecase` (`src/test`), pacote espelhando o main: `br.com.cashflow.usecase.<fluxo>.adapter.driven.persistence`
- Sufixo: `*PersistenceAdapterTest`
- Usar **MockK** para mockar o `Repository`; sem Spring context
- Validar: delegação correta ao Repository, parâmetros passados, retorno propagado
- Para adapters com lógica além de delegação: cobrir branches e transformações
- **Testes de integração:** cobertura indireta via testes de Service no módulo `tests` (já existente); não substitui os unitários

## DTOs (adapter.external.dto)

- **DTOs com lógica** (métodos próprios): testes unitários diretos no pacote `adapter.external.dto`, classe `*DtoTest`
- **DTOs sem lógica** (data class puro): cobertura via testes das **extension functions** de mapeamento (`toResponse`, etc.) — que ficam nos arquivos de DTO
- Criar testes para extension functions em `*ResponseTest` ou `*MapperTest` no mesmo pacote do DTO, exercitando `Entity.toResponse(...)` e validando campos mapeados

## Localização

- Testes no módulo `cashflow-service-usecase`, em `src/test/kotlin`, espelhando a estrutura de `src/main`

## Stack

- JUnit 5, MockK, AssertJ; sem Spring

## Nomenclatura

- Em inglês; backticks permitidos em Kotlin

## Exemplo: Persistence Adapter

```kotlin
@ExtendWith(MockKExtension::class)
class ExamplePersistenceAdapterTest {
    @MockK
    private lateinit var exampleRepository: ExampleRepository
    private lateinit var adapter: ExamplePersistenceAdapter

    @BeforeEach
    fun setUp() { adapter = ExamplePersistenceAdapter(exampleRepository) }

    @Test
    fun `findById delegates to repository and returns entity`() { ... }
}
```

## Exemplo: DTO / extension function

```kotlin
class ExampleResponseTest {
    @Test
    fun `toResponse maps all fields correctly`() {
        val example = Example(...)
        val result = example.toResponse("http://localhost:8081")
        assertThat(result.id).isEqualTo(example.id)
        // ...
    }
}
```
