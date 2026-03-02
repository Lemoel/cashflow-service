# PADRÕES REST/RESTful - TODAS AS APIS
## Sistema de Gestão Escolar - Guia de Implementação

**Versão:** 1.0  
**Data:** 24/02/2026  
**Aplicável a:** Todas as Sprints (1-7)

**Contexto:** Este serviço é **apenas backend** (API REST). Todas as APIs seguem REST nível 3 (Richardson Maturity Model). Em desenvolvimento, base URL: `http://localhost:8081` (porta 8081).

---

## 📚 ÍNDICE

1. [Princípios REST Obrigatórios](#princípios-rest-obrigatórios)
2. [Padrões de URIs](#padrões-de-uris)
3. [Métodos HTTP e Status Codes](#métodos-http-e-status-codes)
4. [Estrutura de Responses com HATEOAS](#estrutura-de-responses-com-hateoas)
5. [Tratamento de Erros](#tratamento-de-erros)
6. [Mapa Completo de Endpoints](#mapa-completo-de-endpoints)
7. [Exemplos de Implementação](#exemplos-de-implementação)

---

## 🌐 PRINCÍPIOS REST OBRIGATÓRIOS

### **Richardson Maturity Model - Level 3**

Todas as APIs devem atingir **Level 3** do modelo de maturidade REST:

| Level | Descrição | Status |
|-------|-----------|--------|
| **Level 0** | HTTP como transporte | ✅ Base |
| **Level 1** | Recursos com URIs | ✅ Obrigatório |
| **Level 2** | Verbos HTTP + Status corretos | ✅ Obrigatório |

---

## 🔗 PADRÕES DE URIs

### **1. Estrutura Base**

```
/api/v{version}/{recurso}[/{id}][/{sub-recurso}]
```

**Exemplos:**
```
/api/v1/usuarios
/api/v1/usuarios/{id}
/api/v1/turmas/{id}/alunos
/api/v1/turmas/{id}/professores
```

### **2. Regras de Nomenclatura**

✅ **USAR:**
- Substantivos no **plural**
- **Minúsculas** (kebab-case se necessário)
- Hierarquia para sub-recursos

```
✅ /api/v1/usuarios
✅ /api/v1/faixas-etarias
✅ /api/v1/turmas/{id}/alunos
```

❌ **NÃO USAR:**
- Verbos nas URIs
- CamelCase ou PascalCase
- Singular

```
❌ /api/v1/getUsuarios
❌ /api/v1/Usuarios
❌ /api/v1/usuario
```

### **3. Query Parameters para Filtros**

```
GET /api/v1/alunos?status=ATIVO
GET /api/v1/alunos?turmaId={uuid}
GET /api/v1/presencas?dataInicio=2026-01-01&dataFim=2026-03-31
```

### **4. Versionamento**

- Versionamento via URI: `/api/v1/`, `/api/v2/`
- Sempre começar com `v1`
- Manter versões antigas funcionando (deprecação gradual)

---

## 📝 MÉTODOS HTTP E STATUS CODES

### **Métodos HTTP Obrigatórios**

| Método | Uso | Idempotente | Safe | Body Request | Body Response |
|--------|-----|-------------|------|--------------|---------------|
| **GET** | Buscar recurso(s) | ✅ | ✅ | ❌ Não | ✅ Sim |
| **POST** | Criar recurso | ❌ | ❌ | ✅ Sim | ✅ Sim |
| **PUT** | Substituir recurso completo | ✅ | ❌ | ✅ Sim | ✅ Sim |
| **PATCH** | Atualizar parcialmente | ❌ | ❌ | ✅ Sim | ✅ Sim |
| **DELETE** | Remover recurso | ✅ | ❌ | ❌ Não | ❌ Não |

### **Status Codes Obrigatórios**

#### **2xx - Sucesso**

| Status | Quando Usar | Response Body |
|--------|-------------|---------------|
| **200 OK** | GET, PUT, PATCH bem-sucedidos | ✅ Sim (recurso atualizado) |
| **201 Created** | POST bem-sucedido | ✅ Sim + Header `Location` |
| **204 No Content** | DELETE bem-sucedido ou PUT sem retorno | ❌ Não |

#### **4xx - Erro do Cliente**

| Status | Quando Usar | Exemplo |
|--------|-------------|---------|
| **400 Bad Request** | Validação falhou ou regra de negócio violada | Campos inválidos, senha muito curta |
| **401 Unauthorized** | Não autenticado | Token/sessão inválida ou ausente |
| **403 Forbidden** | Autenticado mas sem permissão | Professor tentando acessar /usuarios |
| **404 Not Found** | Recurso não existe | GET /usuarios/{id} inexistente |
| **409 Conflict** | Conflito com estado atual | Login duplicado, aluno já matriculado |
| **422 Unprocessable Entity** | Entidade semanticamente incorreta | Idade fora da faixa etária |

#### **5xx - Erro do Servidor**

| Status | Quando Usar |
|--------|-------------|
| **500 Internal Server Error** | Erro inesperado (exception não tratada) |
| **503 Service Unavailable** | Serviço temporariamente indisponível |

---

## 📦 ESTRUTURA DE RESPONSES COM HATEOAS

### **1. Recurso Individual (GET, POST, PUT)**

```json
{
  "id": "uuid",
  "campo1": "valor1",
  "campo2": "valor2",
  "criadoEm": "2026-02-24T20:30:00",
  "atualizadoEm": "2026-02-24T20:30:00",
  "_links": {
    "self": {
      "href": "/api/v1/recurso/{id}",
      "method": "GET"
    },
    "update": {
      "href": "/api/v1/recurso/{id}",
      "method": "PUT"
    },
    "delete": {
      "href": "/api/v1/recurso/{id}",
      "method": "DELETE"
    },
    "relacao1": {
      "href": "/api/v1/recurso/{id}/sub-recurso",
      "method": "GET"
    }
  }
}
```

### **2. Lista de Recursos (GET collection)**

```json
{
  "items": [
    {
      "id": "uuid",
      "campo": "valor",
      "_links": {
        "self": { "href": "/api/v1/recurso/{id}" }
      }
    }
  ],
  "total": 10,
  "page": 1,
  "pageSize": 10,
  "_links": {
    "self": { "href": "/api/v1/recurso?page=1" },
    "next": { "href": "/api/v1/recurso?page=2" },
    "prev": { "href": "/api/v1/recurso?page=0" },
    "create": { 
      "href": "/api/v1/recurso",
      "method": "POST"
    }
  }
}
```

### **3. Response de POST (201 Created)**

**Headers:**
```
HTTP/1.1 201 Created
Location: /api/v1/recurso/{id}
Content-Type: application/json
```

**Body:**
```json
{
  "id": "uuid",
  "campo": "valor",
  "_links": {
    "self": { "href": "/api/v1/recurso/{id}" }
  }
}
```

### **4. Response de DELETE (204 No Content)**

**Headers:**
```
HTTP/1.1 204 No Content
```

**Body:** *(vazio)*

---

## ⚠️ TRATAMENTO DE ERROS

### **Estrutura de Error Response**

Todas as APIs devem retornar erros no seguinte formato:

```json
{
  "timestamp": "2026-02-24T20:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Nome deve ter entre 3 e 200 caracteres",
  "path": "/api/v1/usuarios",
  "details": [
    {
      "field": "nome",
      "message": "deve ter entre 3 e 200 caracteres"
    }
  ]
}
```

### **Mapeamento de Exceções para Status**

| Exceção | Status | Error Type |
|---------|--------|------------|
| `ResourceNotFoundException` | 404 | Not Found |
| `BusinessException` (duplicação) | 409 | Conflict |
| `BusinessException` (validação) | 400 | Bad Request |
| `MethodArgumentNotValidException` | 400 | Validation Error |
| `AccessDeniedException` | 403 | Forbidden |
| `AuthenticationException` | 401 | Unauthorized |
| `Exception` (genérica) | 500 | Internal Server Error |

---

## 🗺️ MAPA COMPLETO DE ENDPOINTS

### **SPRINT 1: Autenticação e Usuários**

#### **Autenticação**
```
POST   /api/v1/auth/login              → 200 | 401
POST   /api/v1/auth/logout             → 204
GET    /api/v1/auth/me                 → 200 | 401
```

#### **Usuários**
```
GET    /api/v1/usuarios                → 200 (lista)
GET    /api/v1/usuarios/{id}           → 200 | 404
POST   /api/v1/usuarios                → 201 | 400 | 409
PUT    /api/v1/usuarios/{id}           → 200 | 400 | 404 | 409
DELETE /api/v1/usuarios/{id}           → 204 | 400 | 404
```

---

### **SPRINT 2: Cadastros Básicos**

#### **Faixas Etárias**
```
GET    /api/v1/faixas-etarias          → 200
GET    /api/v1/faixas-etarias/{id}     → 200 | 404
POST   /api/v1/faixas-etarias          → 201 | 400 | 409
PUT    /api/v1/faixas-etarias/{id}     → 200 | 400 | 404
DELETE /api/v1/faixas-etarias/{id}     → 204 | 404
```

#### **Turmas**
```
GET    /api/v1/turmas                  → 200
GET    /api/v1/turmas/{id}             → 200 | 404
POST   /api/v1/turmas                  → 201 | 400
PUT    /api/v1/turmas/{id}             → 200 | 400 | 404
DELETE /api/v1/turmas/{id}             → 204 | 404

# Sub-recursos
GET    /api/v1/turmas/{id}/alunos      → 200
GET    /api/v1/turmas/{id}/professores → 200
```

#### **Professores**
```
GET    /api/v1/professores             → 200
GET    /api/v1/professores/{id}        → 200 | 404
POST   /api/v1/professores             → 201 | 400 | 409
PUT    /api/v1/professores/{id}        → 200 | 400 | 404
DELETE /api/v1/professores/{id}        → 204 | 404

# Sub-recursos
GET    /api/v1/professores/{id}/turmas → 200
```

#### **Vinculação Professor-Turma**
```
POST   /api/v1/turmas/{turmaId}/professores/{professorId}  → 201 | 400 | 404
DELETE /api/v1/turmas/{turmaId}/professores/{professorId}  → 204 | 404
PUT    /api/v1/turmas/{turmaId}/professores/{professorId}  → 200 (atualizar datas)
```

---

### **SPRINT 3: Gestão de Alunos**

#### **Alunos**
```
GET    /api/v1/alunos                  → 200
GET    /api/v1/alunos/{id}             → 200 | 404
POST   /api/v1/alunos                  → 201 | 400 | 422 (faixa etária)
PUT    /api/v1/alunos/{id}             → 200 | 400 | 404
DELETE /api/v1/alunos/{id}             → 204 | 404

# Filtros
GET    /api/v1/alunos?status=ATIVO     → 200
GET    /api/v1/alunos?turmaId={uuid}   → 200
```

#### **Transferências**
```
POST   /api/v1/alunos/{id}/transferir  → 200 | 400 | 404 | 422
       Body: { turmaDestinoId, data, motivo }
```

#### **Histórico**
```
GET    /api/v1/alunos/{id}/historico   → 200 | 404
```

---

### **SPRINT 4: Calendário e Aulas**

#### **Calendário Trimestral**
```
GET    /api/v1/calendario              → 200
GET    /api/v1/calendario/{ano}        → 200
GET    /api/v1/calendario/{ano}/{trimestre}  → 200 | 404
PUT    /api/v1/calendario/{ano}/{trimestre}  → 200 | 400
       Body: { diaSemana }
```

#### **Aulas**
```
GET    /api/v1/aulas?turmaId={uuid}&trimestre={n}  → 200
GET    /api/v1/aulas/{id}              → 200 | 404
GET    /api/v1/turmas/{turmaId}/aulas  → 200
```

---

### **SPRINT 5: Controle de Presença**

#### **Presença**
```
GET    /api/v1/presencas?turmaId={uuid}&data={date}     → 200
POST   /api/v1/presencas/registrar     → 201 | 400
       Body: { aulaTurmaId, presencas: [{alunoId, presente}] }
PUT    /api/v1/presencas/{id}          → 200 | 400 (até 1 mês)
GET    /api/v1/aulas/{aulaId}/presencas → 200
```

#### **Listas de Presença (PDF)**
```
GET    /api/v1/listas-presenca?turmaId={uuid}&trimestre={n}  → 200 (PDF)
       Accept: application/pdf
```

---

### **SPRINT 6-7: Relatórios**

#### **Relatórios de Frequência**
```
GET    /api/v1/relatorios/frequencia/aluno/{id}?periodo={trimestre}  → 200
GET    /api/v1/relatorios/frequencia/turma/{id}?periodo={trimestre}  → 200
GET    /api/v1/relatorios/frequencia/baixa?limite=75&trimestre={n}   → 200
GET    /api/v1/relatorios/frequencia/anual?ano={year}                → 200
```

#### **Histórico de Turmas**
```
GET    /api/v1/relatorios/historico/turma/{id}?periodo={trimestre}  → 200
```

---

## 💻 EXEMPLOS DE IMPLEMENTAÇÃO

### **1. Controller Padrão**

```kotlin
@RestController
@RequestMapping("/v1/alunos")
@PreAuthorize("hasAnyRole('SECRETARIA', 'TESOURARIA', 'DIRETOR')")
class AlunoController(
    private val alunoInputPort: AlunoInputPort,
    @Value("\${server.servlet.context-path:/api}") private val contextPath: String
) {
    private val baseUrl: String get() = "$contextPath/v1"
    
    /**
     * GET /api/v1/alunos
     * Lista todos os alunos com filtros opcionais
     */
    @GetMapping
    fun listar(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) turmaId: UUID?
    ): ResponseEntity<AlunoListResponse> {
        val alunos = alunoInputPort.findAll(status, turmaId)
            .map { it.toResponse(baseUrl) }
        
        return ResponseEntity.ok(
            AlunoListResponse(
                items = alunos,
                total = alunos.size,
                _links = mapOf(
                    "self" to Link(href = "$baseUrl/alunos"),
                    "create" to Link(href = "$baseUrl/alunos", method = "POST")
                )
            )
        )
    }
    
    /**
     * GET /api/v1/alunos/{id}
     * Busca aluno por ID
     */
    @GetMapping("/{id}")
    fun buscar(@PathVariable id: UUID): ResponseEntity<AlunoResponse> {
        val aluno = alunoInputPort.findById(id)
        return ResponseEntity.ok(aluno.toResponse(baseUrl))
    }
    
    /**
     * POST /api/v1/alunos
     * Cria novo aluno
     */
    @PostMapping
    fun criar(@Valid @RequestBody request: AlunoCreateRequest): ResponseEntity<AlunoResponse> {
        val aluno = alunoInputPort.create(
            nome = request.nome,
            telefone = request.telefone,
            dataNascimento = request.dataNascimento,
            turmaId = request.turmaId
        )
        
        val location: URI = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(aluno.id)
            .toUri()
        
        return ResponseEntity
            .created(location)
            .body(aluno.toResponse(baseUrl))
    }
    
    /**
     * PUT /api/v1/alunos/{id}
     * Atualiza aluno
     */
    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AlunoUpdateRequest
    ): ResponseEntity<AlunoResponse> {
        val aluno = alunoInputPort.update(id, request)
        return ResponseEntity.ok(aluno.toResponse(baseUrl))
    }
    
    /**
     * DELETE /api/v1/alunos/{id}
     * Remove aluno (soft delete)
     */
    @DeleteMapping("/{id}")
    fun desativar(@PathVariable id: UUID): ResponseEntity<Void> {
        alunoInputPort.delete(id)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * GET /api/v1/alunos/{id}/historico
     * Retorna histórico de turmas do aluno
     */
    @GetMapping("/{id}/historico")
    fun historico(@PathVariable id: UUID): ResponseEntity<HistoricoResponse> {
        val historico = alunoInputPort.getHistorico(id)
        return ResponseEntity.ok(historico)
    }
    
    /**
     * POST /api/v1/alunos/{id}/transferir
     * Transfere aluno para outra turma
     */
    @PostMapping("/{id}/transferir")
    fun transferir(
        @PathVariable id: UUID,
        @Valid @RequestBody request: TransferenciaRequest
    ): ResponseEntity<AlunoResponse> {
        val aluno = alunoInputPort.transferir(
            alunoId = id,
            turmaDestinoId = request.turmaDestinoId,
            data = request.data,
            motivo = request.motivo
        )
        return ResponseEntity.ok(aluno.toResponse(baseUrl))
    }
}
```

### **2. DTO com HATEOAS**

```kotlin
data class AlunoResponse(
    val id: UUID,
    val nome: String,
    val telefone: String,
    val dataNascimento: LocalDate,
    val turmaAtual: TurmaResumo,
    val status: String,
    val criadoEm: LocalDateTime,
    val atualizadoEm: LocalDateTime,
    val _links: Map<String, Link>
)

data class TurmaResumo(
    val id: UUID,
    val nome: String,
    val _links: Map<String, Link>
)

data class Link(
    val href: String,
    val method: String? = "GET"
)

fun Aluno.toResponse(baseUrl: String): AlunoResponse {
    return AlunoResponse(
        id = this.id!!,
        nome = this.nome,
        telefone = this.telefone,
        dataNascimento = this.dataNascimento,
        turmaAtual = TurmaResumo(
            id = this.turmaAtualId!!,
            nome = this.turmaAtualNome,
            _links = mapOf(
                "self" to Link(href = "$baseUrl/turmas/${this.turmaAtualId}")
            )
        ),
        status = this.status.name,
        criadoEm = this.criadoEm,
        atualizadoEm = this.atualizadoEm,
        _links = mapOf(
            "self" to Link(href = "$baseUrl/alunos/${this.id}"),
            "update" to Link(href = "$baseUrl/alunos/${this.id}", method = "PUT"),
            "delete" to Link(href = "$baseUrl/alunos/${this.id}", method = "DELETE"),
            "historico" to Link(href = "$baseUrl/alunos/${this.id}/historico"),
            "transferir" to Link(href = "$baseUrl/alunos/${this.id}/transferir", method = "POST"),
            "turma" to Link(href = "$baseUrl/turmas/${this.turmaAtualId}")
        )
    )
}
```

### **3. Exception Handler Completo**

```kotlin
@RestControllerAdvice
class GlobalExceptionAdvice {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                status = 404,
                error = "Not Found",
                message = ex.message ?: "Recurso não encontrado",
                path = request.requestURI
            ))
    }
    
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val message = ex.message ?: "Erro de negócio"
        
        // Detectar tipo de erro
        val status = when {
            message.contains("já está em uso", ignoreCase = true) -> HttpStatus.CONFLICT
            message.contains("duplicado", ignoreCase = true) -> HttpStatus.CONFLICT
            message.contains("já existe", ignoreCase = true) -> HttpStatus.CONFLICT
            message.contains("faixa etária", ignoreCase = true) -> HttpStatus.UNPROCESSABLE_ENTITY
            else -> HttpStatus.BAD_REQUEST
        }
        
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = request.requestURI
            ))
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map {
            FieldError(field = it.field, message = it.defaultMessage ?: "Inválido")
        }
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = 400,
                error = "Validation Error",
                message = "Erro de validação nos campos",
                path = request.requestURI,
                details = details
            ))
    }
    
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(
                status = 403,
                error = "Forbidden",
                message = "Você não tem permissão para acessar este recurso",
                path = request.requestURI
            ))
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Erro inesperado: ${ex.message}", ex)
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                status = 500,
                error = "Internal Server Error",
                message = "Erro interno do servidor. Contate o suporte.",
                path = request.requestURI
            ))
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: List<FieldError>? = null
)

data class FieldError(
    val field: String,
    val message: String
)
```

---

## ✅ CHECKLIST DE VALIDAÇÃO REST

Para cada endpoint implementado, validar:

- [ ] URI segue padrão `/api/v1/{recurso}`
- [ ] Usa método HTTP correto (GET/POST/PUT/DELETE)
- [ ] Retorna status HTTP semântico correto
- [ ] POST retorna 201 Created + header Location
- [ ] DELETE retorna 204 No Content
- [ ] Response inclui `_links` (HATEOAS)
- [ ] Erros retornam ErrorResponse padronizado
- [ ] Content-Type: application/json
- [ ] Validações com Jakarta Validation
- [ ] Documentado com comentários JavaDoc/KDoc
- [ ] Testes validam status e estrutura JSON
- [ ] CORS configurado se necessário

---

## 🎯 RESUMO EXECUTIVO

**Todas as APIs do sistema devem:**

1. ✅ Usar URIs RESTful (`/api/v1/recursos`)
2. ✅ Implementar HATEOAS (`_links` nos responses)
3. ✅ Retornar status HTTP semânticos corretos
4. ✅ POST → 201 Created + Location header
5. ✅ DELETE → 204 No Content
6. ✅ Erros padronizados (ErrorResponse)
7. ✅ Validação com Jakarta Bean Validation
8. ✅ Seguir arquitetura hexagonal (Controller → InputPort)
9. ✅ Testes validando status e estrutura
10. ✅ Documentação clara dos endpoints

---

**Este documento deve ser referência para todas as sprints do projeto.**

**Aprovações:**

| Papel | Nome | Data | Assinatura |
|-------|------|------|------------|
| Tech Lead | __________ | ___/___/___ | __________ |
| Arquiteto | __________ | ___/___/___ | __________ |
