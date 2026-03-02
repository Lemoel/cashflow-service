# API Turmas (School Classes) – Backend Kotlin

**Base URL (dev):** `http://localhost:8081`  
**Prefixo:** `/api/v1/school-classes`  
**Autorização:** Todas as rotas exigem autenticação e uma das roles: `SECRETARIA`, `TESOURARIA`, `DIRETOR`. Enviar cookies de sessão (ex.: após login) ou o header de autenticação usado pelo backend.

**Convenções:**
- Datas (`createdAt`, `updatedAt`): ISO-8601 (ex.: `2026-02-27T15:30:00`)
- IDs: UUID
- Mensagens de erro: inglês

---

## 1. Listar turmas

**GET** `/api/v1/school-classes`

**Query params (opcional):**
- `ageRangeId` (UUID) – filtra por faixa etária

**Response 200 – body:** `TurmaListResponse`

```json
{
  "items": [
    {
      "id": "uuid",
      "name": "string",
      "description": "string | null",
      "ageRange": { "id": "uuid", "name": "string" },
      "active": true,
      "createdAt": "date-time ou null",
      "updatedAt": "date-time ou null",
      "statistics": null,
      "_links": {
        "self": { "href": "string", "method": "GET" },
        "update": { "href": "string", "method": "PUT" },
        "delete": { "href": "string", "method": "DELETE" },
        "students": { "href": "string", "method": "GET" },
        "teachers": { "href": "string", "method": "GET" },
        "ageRange": { "href": "string", "method": "GET" }
      }
    }
  ],
  "total": 0,
  "_links": {
    "self": { "href": "string", "method": "GET" },
    "create": { "href": "string", "method": "POST" }
  }
}
```

**Exemplo curl:**

```bash
curl -X GET "http://localhost:8081/api/v1/school-classes" \
  -H "Accept: application/json" \
  --cookie "JSESSIONID=..."

# Com filtro por faixa etária
curl -X GET "http://localhost:8081/api/v1/school-classes?ageRangeId=UUID_DA_FAIXA" \
  -H "Accept: application/json" \
  --cookie "JSESSIONID=..."
```

---

## 2. Buscar turma por ID

**GET** `/api/v1/school-classes/{id}`

**Path:** `id` (UUID)

**Response 200 – body:** `TurmaResponse` (igual ao item de `items` acima, com `statistics` preenchido)

```json
{
  "id": "uuid",
  "name": "string",
  "description": "string | null",
  "ageRange": { "id": "uuid", "name": "string" },
  "active": true,
  "createdAt": "date-time ou null",
  "updatedAt": "date-time ou null",
  "statistics": {
    "totalStudents": 0
  },
  "_links": { ... }
}
```

**Exemplo curl:**

```bash
curl -X GET "http://localhost:8081/api/v1/school-classes/SEU_UUID" \
  -H "Accept: application/json" \
  --cookie "JSESSIONID=..."
```

---

## 3. Criar turma

**POST** `/api/v1/school-classes`

**Request body:** `TurmaCreateRequest` (JSON)

| Campo         | Tipo    | Obrigatório | Regras / Observação                          |
|---------------|---------|-------------|----------------------------------------------|
| name          | string  | sim         | 3–100 caracteres                             |
| description   | string  | não         | máx. 500 caracteres; pode ser `null`         |
| ageRangeId    | UUID    | sim         | ID de faixa etária existente e ativa          |
| active        | boolean | não         | default true                                  |

**Exemplo de body:**

```json
{
  "name": "Maternal A",
  "description": "Turma do período matutino",
  "ageRangeId": "uuid-da-faixa-etaria",
  "active": true
}
```

**Response 201** – header `Location: .../api/v1/school-classes/{id}` e body `TurmaResponse` (sem `statistics`).

**Exemplo curl:**

```bash
curl -X POST "http://localhost:8081/api/v1/school-classes" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  --cookie "JSESSIONID=..." \
  -d '{
    "name": "Maternal A",
    "description": "Turma do período matutino",
    "ageRangeId": "UUID_DA_FAIXA_ETARIA",
    "active": true
  }'
```

---

## 4. Atualizar turma

**PUT** `/api/v1/school-classes/{id}`

**Path:** `id` (UUID)

**Request body:** `TurmaUpdateRequest` (JSON) – mesmos campos do create, todos obrigatórios no update:

| Campo        | Tipo    | Obrigatório |
|-------------|---------|-------------|
| name        | string  | sim (3–100) |
| description | string  | pode null   |
| ageRangeId  | UUID    | sim         |
| active      | boolean | sim         |

**Exemplo de body:**

```json
{
  "name": "Maternal A",
  "description": "Turma do período matutino",
  "ageRangeId": "uuid-da-faixa-etaria",
  "active": true
}
```

**Response 200** – body `TurmaResponse` (com `statistics`).

**Exemplo curl:**

```bash
curl -X PUT "http://localhost:8081/api/v1/school-classes/SEU_UUID" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  --cookie "JSESSIONID=..." \
  -d '{
    "name": "Maternal A",
    "description": "Turma do período matutino",
    "ageRangeId": "UUID_DA_FAIXA_ETARIA",
    "active": true
  }'
```

---

## 5. Excluir turma

**DELETE** `/api/v1/school-classes/{id}`

**Path:** `id` (UUID)

**Response 204** – sem body. Não é permitido excluir turma com alunos matriculados (erro 400 com mensagem em inglês, ex.: "Cannot delete school class '...' because it has enrolled students").

**Exemplo curl:**

```bash
curl -X DELETE "http://localhost:8081/api/v1/school-classes/SEU_UUID" \
  -H "Accept: application/json" \
  --cookie "JSESSIONID=..."
```

---

## 6. Estatísticas da turma

**GET** `/api/v1/school-classes/{id}/statistics`

**Path:** `id` (UUID)

**Response 200 – body:** `DetailedStatisticsResponse`

```json
{
  "classId": "uuid",
  "className": "string",
  "totalStudents": 0,
  "_links": {
    "self": { "href": "string", "method": "GET" },
    "schoolClass": { "href": "string", "method": "GET" },
    "students": { "href": "string", "method": "GET" },
    "teachers": { "href": "string", "method": "GET" }
  }
}
```

**Exemplo curl:**

```bash
curl -X GET "http://localhost:8081/api/v1/school-classes/SEU_UUID/statistics" \
  -H "Accept: application/json" \
  --cookie "JSESSIONID=..."
```

---

## Resumo dos contratos

| Método | Endpoint                            | Body (quando aplicável)   | Resposta principal           |
|--------|-------------------------------------|----------------------------|------------------------------|
| GET    | `/api/v1/school-classes`                    | –                          | `TurmaListResponse`          |
| GET    | `/api/v1/school-classes/{id}`               | –                          | `TurmaResponse`              |
| POST   | `/api/v1/school-classes`                    | `TurmaCreateRequest`       | 201 + `TurmaResponse`        |
| PUT    | `/api/v1/school-classes/{id}`               | `TurmaUpdateRequest`       | `TurmaResponse`              |
| DELETE | `/api/v1/school-classes/{id}`               | –                          | 204 No Content               |
| GET    | `/api/v1/school-classes/{id}/statistics`  | –                          | `DetailedStatisticsResponse` |

**Erros comuns:** 400 (validação ou regra de negócio), 404 (turma/faixa não encontrada). O backend devolve JSON com `message` (e opcionalmente `details`) em **inglês**.

**Formato de erro (4xx/5xx):**

```json
{
  "timestamp": "2026-02-27T15:30:00",
  "status": 400,
  "message": "Name is required",
  "path": "/api/v1/school-classes",
  "details": [
    { "field": "name", "message": "Name is required" }
  ],
  "error": "Bad Request"
}
```

**Mensagens de validação (create/update):**

| Campo       | Mensagem                                      |
|-------------|-----------------------------------------------|
| name        | "Name is required"                            |
| name        | "Name must be between 3 and 100 characters"   |
| description | "Description must be at most 500 characters"   |
| ageRangeId  | "Age range is required"                        |

**Mensagens de regra de negócio:**

| Situação                          | Status | Mensagem                                                                 |
|-----------------------------------|--------|---------------------------------------------------------------------------|
| Turma não encontrada              | 404    | "School class not found with ID: {id}"                                   |
| Nome duplicado                    | 400    | "A school class with this name already exists: '{name}'"                 |
| Faixa etária inativa              | 400    | "Age range '{name}' is inactive"                                         |
| Excluir turma com alunos          | 400    | "Cannot delete school class '{name}' because it has enrolled students"    |
