# Analise do Fluxo Extrair Movimentos PagBank (/api/movimentos/extrair)

## 1. Contexto e Objetivo

- **Nome do fluxo:** Extrair Movimentos PagBank (`/api/movimentos/extrair`)
- **Sistema atual (legado):** Sistema de Eventos - modulos `financial-connectors` + `persistence-database` + `business-logic` + `ui`
- **Objetivo da analise:**
  Descrever, de forma detalhada, o funcionamento atual do fluxo de extracao automatica de movimentos financeiros da API EDI PagBank, incluindo chamadas a API externa, processamento de dados, criacao/atualizacao de entidades e regras de negocio, para apoiar o planejamento da codificacao no novo projeto `cashflow-service` (Kotlin).

- **Escopo da analise:**
  - Entender o fluxo ponta a ponta (trigger HTTP ate gravacao em banco).
  - Mapear a integracao com a API externa PagBank EDI.
  - Identificar e documentar todas as regras de negocio.
  - Descrever entidades e seus relacionamentos.
  - Documentar tratamento de erros e mecanismos de resiliencia.

- **Fora de escopo:**
  - Planejamento de migracao de dados (ja realizado).
  - Definicao de arquitetura de alto nivel (ports, adapters, etc.), que ja esta pre-definida no novo projeto.
  - Frontend (nao ha frontend para este fluxo; e disparado por Cloud Scheduler GCP).

---

## 2. Descricao Geral da Funcionalidade

- **Resumo da funcionalidade:**
  O fluxo "Extrair Movimentos" e responsavel por buscar automaticamente, via API, os movimentos financeiros (transacoes de cartao de credito, debito, PIX, boleto, etc.) processados pelo PagBank. O sistema consulta a API EDI do PagBank dia a dia, desde a ultima data processada ate o dia anterior (D-1), deserializa as respostas JSON, criptografa e armazena o payload bruto na tabela `movimento_api`, e em seguida processa cada transacao individual, criando registros na tabela `lancamento`. Maquinas de cartao (terminais POS) sao criadas automaticamente quando referenciadas em transacoes e ainda nao existem no sistema. O fluxo e projetado para ser idempotente e resiliente, com deduplicacao de lancamentos via constraint de unicidade e retry automatico em falhas de conexao.

- **Quem utiliza o fluxo (perfil de usuario):**
  - **Cloud Scheduler (GCP):** Trigger automatico via chamada HTTP agendada (principal consumidor).
  - **Administrador do sistema:** Pode disparar manualmente para reprocessamento ou extracao de dia especifico.

- **Cenarios principais de uso:**
  1. **Extracao automatica diaria** - Cloud Scheduler dispara `GET /api/movimentos/extrair`, o sistema busca todos os dias pendentes desde a ultima extracao ate D-1.
  2. **Extracao de dia especifico** - Chamada manual a `GET /api/movimentos/lacamentoDoDia?data=YYYY-MM-DD` para extrair movimentos de uma data especifica.
  3. **Reprocessamento** - O metodo `reprocessar(UUID movimentoID)` permite reprocessar um movimento ja salvo, descriptografando o payload e re-executando o processamento de lancamentos.

---

## 3. Fluxo Detalhado da Funcionalidade

### 3.1. Visao Geral do Fluxo

- **Ponto de entrada:** `GET /api/movimentos/extrair` chamado por Cloud Scheduler GCP com header `X-API-KEY`.

- **Passo a passo (alto nivel):**
  1. Autenticacao via `X-API-KEY` ou sessao `JSESSIONID`.
  2. Buscar ultimo evento processado no banco de dados.
  3. Calcular range de datas pendentes (ultima data processada ate D-1).
  4. Para cada dia pendente (em paralelo, max 3 concorrentes):
     a. Chamar API PagBank EDI com a data.
     b. Validar header `validado` da resposta.
     c. Deserializar JSON e salvar payload criptografado na tabela `movimento_api`.
     d. Se houver paginacao (totalPages > 1), buscar paginas adicionais sequencialmente.
     e. Combinar todas as transacoes do dia.
     f. Processar transacoes: buscar/criar maquinas, inserir lancamentos.
     g. Atualizar status do movimento para `PROCESSADA`.

- **Diagrama do fluxo:**

```
Cloud Scheduler GCP
    |
    | GET /api/movimentos/extrair (X-API-KEY header)
    v
MovimentoApiController.extrairTransacoes()
    |
    v
MovimentoApiService.extrairMovimentos()
    |
    v
MovimentoApiAdapter.extrairMovimentos()
    |
    |-- 1. buscarUltimoEventoProcessado()
    |       -> MovimentoApiRepository.findFirstByOrderByDataLeituraDesc()
    |
    |-- 2. Calcular range: ultimaData .. D-1
    |
    |-- 3. Para cada dia (Virtual Threads + Semaforo max 3):
    |       |
    |       |-- processarDia(data, pagina)
    |       |       |
    |       |       |-- clienteBancoPort.buscarMovimentos(data, page, pageSize)
    |       |       |       -> PagSeguroClient (Feign) -> https://edi.api.pagbank.com.br/movement/v3.00/transactional/{data}
    |       |       |
    |       |       |-- Validar header "validado" == "true"
    |       |       |
    |       |       |-- Deserializar JSON -> MovimentoApiResponse
    |       |       |-- Criptografar payload (AES-256/GCM)
    |       |       |-- Salvar MovimentoApiEntity (status: RECEBIDO)
    |       |       |
    |       |       |-- Se totalPages > 1: paginar() (paginas 2..N sequencialmente)
    |       |       |
    |       |       |-- Combinar todas as transacoes do dia
    |       |       |
    |       |       |-- converterLancamento() -> LancamentoOutputPort.processarMovimentoApiResponse()
    |       |               |
    |       |               |-- LancamentoAdapter.processarMovimentoApiResponse()
    |       |               |       |
    |       |               |       |-- Buscar MovimentoApiEntity por data e pagina
    |       |               |       |-- ProcessamentoLancamentoTransactional.processarEmTransacao()
    |       |               |               |
    |       |               |               |-- Extrair numeroSerieLeitor unicos
    |       |               |               |-- MaquinaManagementService.findOrCreateMaquinasBatch()
    |       |               |               |-- Para cada lancamento:
    |       |               |               |       salvarIgnorandoDuplicata() (ON CONFLICT DO NOTHING)
    |       |               |               |-- Status = PROCESSADA (ou ERRO_PROCESSAMENTO)
    |       |               |               |-- [finally] Salvar MovimentoApiEntity
    |
    |-- 4. Aguardar todas as tarefas (CompletableFuture.allOf().join())
    |-- 5. Shutdown executor (timeout 60s)
    |
    v
ResponseEntity.accepted() com tempo de execucao
```

### 3.2. Passo a Passo Detalhado

#### Passo 1 - Autenticacao e Trigger

- **Acao do usuario/sistema:** Cloud Scheduler GCP dispara `GET /api/movimentos/extrair` com header `X-API-KEY`.
- **Acao do sistema:** O endpoint e protegido pelo `ApiKeyAuthFilter`, que intercepta todas as requisicoes a `/api/**`.
- **Dados de entrada:** Header HTTP `X-API-KEY` com valor configurado em `eventos.app.api.key`.
- **Validacoes aplicadas:**
  1. Se a requisicao possui cookie `JSESSIONID` valido (nao nulo e nao vazio), permite acesso direto (bypass da API key).
  2. Caso contrario, verifica a presenca do header `X-API-KEY`.
  3. Se o header esta ausente ou vazio: retorna `401 Unauthorized`.
  4. Se o header esta presente mas o valor nao confere: retorna `403 Forbidden`.
- **Erros e mensagens:**
  - HTTP `401 Unauthorized` - Texto: "API Key nao informada"
  - HTTP `403 Forbidden` - Texto: "API Key invalida"
- **Dados de saida:** Acesso permitido ao controller.
- **Configuracao relevante:**
  - `eventos.app.api.key`: chave da API configurada no `application.yml`.

#### Passo 2 - Determinar Range de Datas a Processar

- **Acao do sistema:**
  1. Consulta o banco para buscar o registro mais recente em `movimento_api`, ordenado por `data_leitura DESC` (`findFirstByOrderByDataLeituraDesc()`).
  2. Se nenhum registro for encontrado (ou o `createdAt` for null), utiliza a data configurada em `eventos.bancos.pagbank.inicio` (default: `2025-01-01`) como ponto de partida, convertida para `ZonedDateTime` no fuso `UTC`.
  3. Trunca a data do ultimo evento para o inicio do dia (`truncatedTo(ChronoUnit.DAYS)`).
  4. Calcula D-1 (`ZonedDateTime.now().minusDays(1)`) e tambem trunca para o inicio do dia.
  5. Se a ultima data processada NAO for posterior a D-1, gera a lista de datas do range (inclusivo em ambos os extremos).
  6. Obtem o numero da `pagina` do ultimo evento para usar como pagina inicial do primeiro dia. Se `pagina < 1`, usa 1 como default.
- **Dados de entrada:** Nenhum parametro externo (automatico).
- **Dados de saida:** Lista de `ZonedDateTime` representando cada dia a processar, e pagina inicial.
- **Validacoes:** Se `ultimaDataRecebidaEProcessada.isAfter(dMenos1)`, nao ha dias para processar - apenas loga e encerra.

#### Passo 3 - Processamento Paralelo por Dia

- **Acao do sistema:**
  1. Cria um `ExecutorService` com virtual threads (`Executors.newVirtualThreadPerTaskExecutor()`).
  2. Cria um `Semaphore(3)` para limitar a concorrencia a no maximo 3 dias processados simultaneamente.
  3. Para cada data na lista, submete uma tarefa assincrona via `CompletableFuture.runAsync()`:
     - Adquire o semaforo (`semaforo.acquire()`).
     - Executa `processarDia(data, pagina)`.
     - Libera o semaforo no bloco `finally`.
  4. Aguarda todas as tarefas concluirem com `CompletableFuture.allOf(tarefas).join()`.
  5. Faz shutdown do executor com timeout de 60 segundos. Se nao encerrar nesse prazo, forca com `shutdownNow()`.
- **Dados de entrada:** Lista de datas e pagina inicial.
- **Tratamento de erro:** Erros em dias individuais sao logados (`LOG.error`) mas NAO interrompem o processamento dos demais dias. Cada dia e independente.

#### Passo 4 - Chamada a API PagBank EDI (por dia)

- **Acao do sistema:**
  1. Formata a data como `YYYY-MM-DD` (string).
  2. Chama `clienteBancoPort.buscarMovimentos(dataFormatada, pagina, pageSize)`.
  3. Esta chamada e implementada pelo `PagSeguroClient` (Feign Client).
- **Dados de entrada:**
  - `data`: data no formato `YYYY-MM-DD` (path variable na URL).
  - `pageNumber`: numero da pagina (query param, 1-based).
  - `pageSize`: tamanho da pagina (query param, configuravel, default 1000).
- **API externa:**
  - **URL:** `GET https://edi.api.pagbank.com.br/movement/v3.00/transactional/{data}?pageNumber={page}&pageSize={size}`
  - **Autenticacao:** HTTP Basic Auth.
  - **Credenciais:** `eventos.bancos.pagbank.pags.username` (env: `PAGSEGURO_USERNAME`) e `eventos.bancos.pagbank.pags.password` (env: `PAGSEGURO_PASSWORD`).
- **Dados de saida:** `ResponseEntity<String>` contendo JSON no body e headers HTTP.
- **Validacao critica:** Apos receber a resposta, verifica o header HTTP `validado`:
  - Se `"true"` (case-insensitive via `StringToBooleanConverter`): processa a resposta.
  - Se qualquer outro valor, null ou ausente: ignora silenciosamente toda a resposta (nao salva, nao gera erro).

#### Passo 5 - Deserializacao e Armazenamento do Payload

- **Acao do sistema:**
  1. O JSON do body da resposta e deserializado em `MovimentoApiResponse` usando Jackson `ObjectMapper`.
  2. A estrutura `MovimentoApiResponse` contem:
     - `detalhes`: `List<Lancamento>` - lista de transacoes.
     - `pagination`: `Pagination(totalPages, page, totalElements)` - metadados de paginacao.
  3. O mapeamento JSON->Java usa `@JsonNaming(SnakeCaseStrategy)` no record `Lancamento` (campos da API PagBank em snake_case sao mapeados para camelCase).
  4. Os enums `TipoEventoEnum`, `MeioCapturaEnum` e `MeioPagamentoEnum` usam `@JsonCreator` para converter codigos numericos retornados pela API em valores do enum Java.
  5. O JSON original (nao deserializado) e criptografado via `MovimentoApiResponseConversor.encrypt()`:
     - Algoritmo: AES-256/GCM/NoPadding.
     - IV aleatorio de 12 bytes, concatenado ao ciphertext.
     - Resultado codificado em Base64.
     - Chave AES-256 configurada em `eventos.bancos.encryption.key` (Base64-encoded).
  6. Salva um `MovimentoApiEntity` com:
     - `payload`: JSON criptografado (Base64 string).
     - `status`: `RECEBIDO`.
     - `pagina`: numero da pagina atual.
     - `totalElementos`: vem de `pagination.totalElements`.
     - `totalPaginas`: vem de `pagination.totalPages`.
     - `dataLeitura`: data do dia sendo processado (tipo `Date`, convertido via `DateTimeUtil.toDate()`).
     - `creationUserId`: `"CRON"`.
- **Erros e status associados:**
  - `JsonProcessingException` -> salva `MovimentoApiEntity` com status `ERRO_PAYLOAD` e lanca `RuntimeException`.
  - `DataIntegrityViolationException` -> NAO salva (constraint UNIQUE violada), lanca `RuntimeException`.
  - Qualquer outra `Exception` -> salva `MovimentoApiEntity` com payload criptografado e status `ERRO_INTERNO`, e lanca `RuntimeException`.

#### Passo 6 - Paginacao

- **Acao do sistema:** Se `totalPages > 1` (obtido da paginacao da primeira resposta), busca as paginas restantes (2 ate N) de forma sequencial (nao paralela):
  1. Para cada pagina adicional (2, 3, ..., totalPages):
     a. Chama `clienteBancoPort.buscarMovimentos(dataFormatada, page, pageSize)`.
     b. Valida o header `validado` da resposta.
     c. Se valido, deserializa o JSON e salva como um `MovimentoApiEntity` individual (cada pagina gera um registro separado).
     d. Coleta os `detalhes` (transacoes) daquela pagina.
  2. Apos processar todas as paginas, combina as transacoes de todas as paginas (incluindo a primeira) em uma unica lista.
- **Tratamento de erro:** Se qualquer pagina falhar:
  - Salva um `MovimentoApiEntity` com status `ERRO_COMUNICACAO`.
  - Lanca `RuntimeException`, interrompendo o processamento de todo aquele dia.
  - Dias subsequentes nao sao afetados (cada dia e independente).

#### Passo 7 - Processamento de Lancamentos (Transacional)

- **Acao do sistema:**
  1. Cria um `MovimentoApiResponseEvent` contendo a pagina (1), a data de leitura e o `MovimentoApiResponse` combinado (com todas as transacoes do dia).
  2. Chama `LancamentoOutputPort.processarMovimentoApiResponse(event)`.
  3. O `LancamentoAdapter` busca o `MovimentoApiEntity` correspondente por `dataLeitura` e `pagina` (=1).
  4. Se nao encontrar o `MovimentoApiEntity`, loga warning e retorna sem processar.
  5. Delega para `ProcessamentoLancamentoTransactional.processarEmTransacao()`, que executa em transacao:
     a. **Guarda:** So processa se `detalhes` nao e nulo/vazio E status do `MovimentoApiEntity` e `RECEBIDO`.
     b. Extrai todos os `numeroSerieLeitor` unicos das transacoes (filtrando nulos e vazios).
     c. Chama `MaquinaManagementService.findOrCreateMaquinasBatch(seriesLeitor)` para buscar/criar maquinas em lote.
     d. Para cada `Lancamento` na lista de detalhes:
        - Busca a `MaquinaEntity` correspondente no mapa retornado, usando `lancamento.numeroSerieLeitor()` como chave.
        - Se o lancamento nao tem `numeroSerieLeitor` (nulo ou vazio), `maquina = null`.
        - Insere via `LancamentoRepository.salvarIgnorandoDuplicata()` com todos os campos:
          - `nsu`, `tid`, `codigoTransacao`, `parcela`
          - `tipoEvento` (`.name()` do enum), `meioCaptura` (`.name()`), `meioPagamento` (`.name()`)
          - `valorParcela`, `estabelecimento`, `pagamentoPrazo`, `taxaIntermediacao`
          - `numeroSerieLeitor`, `valorTotalTransacao`, `dataInicialTransacao`, `horaInicialTransacao`
          - `dataPrevistaPagamento`, `valorLiquidoTransacao`, `valorOriginalTransacao`
          - `maquinaId` (da MaquinaEntity ou null)
          - `congregacaoId` (da MaquinaEntity.congregacao ou null)
          - `departamentoId` (da MaquinaEntity.departamento ou null)
          - `creationUserId` = `"BOT"`
        - A query usa `INSERT ... ON CONFLICT (codigo_transacao, tipo_evento, parcela) DO NOTHING` - duplicatas sao ignoradas silenciosamente.
     e. Atualiza status do `MovimentoApiEntity` para `PROCESSADA`.
  6. Em caso de excecao: status atualizado para `ERRO_PROCESSAMENTO`.
  7. No bloco `finally` (sempre executado): atualiza `updatedAt` e salva o `MovimentoApiEntity`.
- **Transacionalidade:**
  - Anotado com `@Transactional`.
  - Retry: 3 tentativas para `CannotCreateTransactionException` e `JDBCConnectionException`, com backoff exponencial (delay=2000ms, multiplier=2).

#### Passo 8 - Criacao Automatica de Maquinas

- **Acao do sistema:**
  1. Recebe um `Set<String>` com os `numeroSerieLeitor` unicos das transacoes do dia.
  2. Busca em lote todas as maquinas existentes via `maquinaRepository.findByNumeroSerieLeitorIn(seriesLeitor)`.
  3. Cria um mapa `numeroSerieLeitor -> MaquinaEntity` com as existentes.
  4. Para cada `numeroSerieLeitor` que NAO esta no mapa, cria uma nova `MaquinaEntity`:
     - `numeroSerieLeitor`: o numero de serie do leitor.
     - `banco`: referencia ao bean `pagBank` (BancoEntity do PagBank, codigo "290").
     - `ativo`: `true`.
     - `creationUserId`: `"BOT"`.
     - `congregacao`: `null` (nao vinculada inicialmente).
     - `departamento`: `null` (nao vinculado inicialmente).
  5. Salva todas as novas maquinas em lote via `maquinaRepository.saveAll()`.
  6. Adiciona as novas maquinas ao mapa e retorna.
- **Transacionalidade:** `@Transactional(propagation = Propagation.REQUIRES_NEW)` - transacao separada da transacao principal de lancamentos.
- **Retry:** 5 tentativas para `ObjectOptimisticLockingFailureException` e `DataIntegrityViolationException`, com backoff exponencial (delay=200ms, multiplier=2).
- **Cache:** `findByNumeroSerieLeitor` e `findByNumeroSerieLeitorIn` sao anotados com `@Cacheable`.
- **Locking:** `findByNumeroSerieLeitor` usa `@Lock(LockModeType.PESSIMISTIC_WRITE)` para evitar race conditions.

---

## 4. Entidades Envolvidas

### 4.1. Lista de Entidades

- **Entidade:** `MovimentoApi` / `MovimentoApiEntity`
  - **Descricao:** Representa uma resposta bruta da API PagBank EDI para um dia e pagina especificos. Armazena o payload JSON criptografado e metadados de processamento (status, paginacao).
  - **Onde e usada no fluxo:** Passos 2, 5, 6, 7 - determinacao do ponto de partida, armazenamento da resposta da API, controle de status de processamento.
  - **Observacoes:** Constraint UNIQUE em `(data_leitura, pagina)` garante um unico registro por dia por pagina. Herda campos de auditoria de `BaseAuditEntity` (`createdAt`, `updatedAt`, `creationUserId`, `modUserId`) e `id` de `BaseEntity`.

- **Entidade:** `Lancamento` / `LancamentoEntity`
  - **Descricao:** Representa uma transacao financeira individual (venda, pagamento, ajuste, chargeback, PIX, etc.) extraida do PagBank.
  - **Onde e usada no fluxo:** Passo 7 - gravacao dos dados financeiros individuais.
  - **Observacoes:** Constraint UNIQUE em `(codigo_transacao, tipo_evento, parcela)` previne insercao de duplicatas. Relaciona-se com `MaquinaEntity`, `CongregacaoEntity` e `DepartamentoEntity`.

- **Entidade:** `Maquina` / `MaquinaEntity`
  - **Descricao:** Representa uma maquina de cartao / terminal POS. E identificada univocamente pelo `numero_serie_leitor`.
  - **Onde e usada no fluxo:** Passo 8 - associacao de transacoes a maquinas fisicas, criacao automatica se inexistente.
  - **Observacoes:** Criada automaticamente durante a extracao se nao existir. Possui controle de versao para concorrencia otimista (`@Version`). Cacheavel. Relaciona-se com `BancoEntity`, `CongregacaoEntity` e `DepartamentoEntity`.

- **Entidade:** `Banco` / `BancoEntity`
  - **Descricao:** Representa um banco ou adquirente. O PagBank (codigo "290") e pre-cadastrado via migration SQL.
  - **Onde e usada no fluxo:** Passo 8 - referencia obrigatoria ao criar nova maquina.
  - **Observacoes:** Carregado como bean Spring (`pagBank`) na inicializacao da aplicacao. Se nao existir no banco, a aplicacao NAO inicia (lanca excecao). NAO herda `BaseAuditEntity`; herda apenas `BaseEntity` (somente `id`).

- **Entidade:** `Congregacao` / `CongregacaoEntity`
  - **Descricao:** Unidade organizacional (congregacao religiosa) que pode ser vinculada a uma maquina.
  - **Onde e usada no fluxo:** Passo 7 - o `congregacao_id` do lancamento e herdado da maquina.
  - **Observacoes:** NAO e criada neste fluxo. A vinculacao maquina-congregacao e feita em outro fluxo (cadastro de maquinas). Pertence a um `TenantEntity` e opcionalmente a uma `setorial` (auto-referencia).

- **Entidade:** `Departamento` / `DepartamentoEntity`
  - **Descricao:** Departamento organizacional vinculado a uma maquina.
  - **Onde e usada no fluxo:** Passo 7 - o `departamento_id` do lancamento e herdado da maquina.
  - **Observacoes:** Mesmo comportamento que `Congregacao` neste fluxo. NAO e criado aqui. Pertence a um `TenantEntity`.

### 4.2. Principais Campos por Entidade

#### Entidade `MovimentoApiEntity`

- **Tabela / Recurso:** `eventos.movimento_api`
- **Descricao geral:** Armazena cada resposta da API PagBank (payload criptografado) e controla o status de processamento.

- **Campos principais:**
  - `id`
    - Tipo: UUID (PK, gerado automaticamente)
    - Obrigatorio: Sim
    - Descricao: Identificador unico do registro
  - `payload`
    - Tipo: TEXT (columnDefinition "jsonb" na entity, mas armazena Base64 criptografado)
    - Obrigatorio: Nao
    - Descricao: JSON da resposta da API PagBank, criptografado com AES-256/GCM e codificado em Base64
    - Restricoes: Pode ser vazio em caso de erro
  - `status`
    - Tipo: VARCHAR(30)
    - Obrigatorio: Sim
    - Descricao: Status do processamento
    - Valores possiveis: `RECEBIDO`, `PROCESSADA`, `ERRO_PAYLOAD`, `ERRO_COMUNICACAO`, `ERRO_PROCESSAMENTO`, `ERRO_INTERNO`
  - `pagina`
    - Tipo: NUMERIC(2)
    - Obrigatorio: Sim
    - Descricao: Numero da pagina da API (1-based)
  - `total_elementos`
    - Tipo: NUMERIC(4)
    - Obrigatorio: Sim
    - Descricao: Total de elementos retornados na pagina
  - `total_paginas`
    - Tipo: NUMERIC(4)
    - Obrigatorio: Sim
    - Descricao: Total de paginas existentes na API para aquele dia
  - `data_leitura`
    - Tipo: DATE
    - Obrigatorio: Sim
    - Descricao: Data dos movimentos sendo extraidos (dia de referencia)
  - `created_at`
    - Tipo: TIMESTAMP WITH TIME ZONE
    - Obrigatorio: Sim (auto-preenchido)
    - Descricao: Data e hora de criacao do registro
  - `updated_at`
    - Tipo: TIMESTAMP WITH TIME ZONE
    - Obrigatorio: Nao (auto-preenchido na atualizacao)
    - Descricao: Data e hora da ultima atualizacao
  - `creation_user_id`
    - Tipo: VARCHAR(255)
    - Obrigatorio: Sim
    - Descricao: Identificador do usuario que criou o registro. Valor fixo: `"CRON"` neste fluxo
  - `mod_user_id`
    - Tipo: VARCHAR(255)
    - Obrigatorio: Nao
    - Descricao: Identificador do usuario que modificou o registro
  - **Constraints:**
    - `uk_data_pagina`: UNIQUE (data_leitura, pagina)
    - `idx_movimento_api_status`: INDEX em status

#### Entidade `LancamentoEntity`

- **Tabela / Recurso:** `eventos.lancamento`
- **Descricao geral:** Transacao financeira individual proveniente da API PagBank EDI.

- **Campos principais:**
  - `id`
    - Tipo: UUID (PK, gerado via `gen_random_uuid()` no INSERT nativo)
    - Obrigatorio: Sim
    - Descricao: Identificador unico da transacao
  - `nsu`
    - Tipo: VARCHAR(30)
    - Obrigatorio: Nao
    - Descricao: Numero Sequencial Unico da transacao
  - `tid`
    - Tipo: VARCHAR(64)
    - Obrigatorio: Nao
    - Descricao: Transaction ID (identificador da transacao no adquirente)
  - `codigo_transacao`
    - Tipo: VARCHAR(33)
    - Obrigatorio: Nao (mas parte da constraint UNIQUE)
    - Descricao: Codigo unico da transacao no PagBank
  - `parcela`
    - Tipo: VARCHAR(5)
    - Obrigatorio: Sim
    - Descricao: Numero da parcela (ex: "01", "02", "03")
  - `tipo_evento`
    - Tipo: VARCHAR(50)
    - Obrigatorio: Sim
    - Descricao: Tipo do evento. Armazenado como NOME do enum Java (ex: `VENDA_OU_PAGAMENTO`, `CHARGEBACK`, `PIX`)
    - Restricoes: Deve corresponder a um valor valido de `TipoEventoEnum`
  - `meio_captura`
    - Tipo: VARCHAR(50)
    - Obrigatorio: Sim
    - Descricao: Meio de captura da transacao. Armazenado como NOME do enum (ex: `CHIP`, `TARJA`, `QR_CODE`)
    - Restricoes: Deve corresponder a um valor valido de `MeioCapturaEnum`
  - `valor_parcela`
    - Tipo: NUMERIC(12,2)
    - Obrigatorio: Sim
    - Descricao: Valor monetario da parcela
  - `meio_pagamento`
    - Tipo: VARCHAR(50)
    - Obrigatorio: Sim
    - Descricao: Meio de pagamento utilizado. Armazenado como NOME do enum (ex: `PIX`, `CARTAO_CREDITO`, `CARTAO_DEBITO`)
    - Restricoes: Deve corresponder a um valor valido de `MeioPagamentoEnum`
  - `estabelecimento`
    - Tipo: VARCHAR(20)
    - Obrigatorio: Sim
    - Descricao: Codigo do estabelecimento no PagBank
  - `pagamento_prazo`
    - Tipo: CHAR(1)
    - Obrigatorio: Sim
    - Descricao: Indicador de prazo de pagamento
  - `taxa_intermediacao`
    - Tipo: NUMERIC(12,2)
    - Obrigatorio: Sim
    - Descricao: Valor da taxa de intermediacao cobrada pelo PagBank
  - `numero_serie_leitor`
    - Tipo: VARCHAR(20)
    - Obrigatorio: Nao
    - Descricao: Numero de serie da maquina de cartao que realizou a transacao
  - `valor_total_transacao`
    - Tipo: NUMERIC(12,2)
    - Obrigatorio: Sim
    - Descricao: Valor bruto total da transacao
  - `data_inicial_transacao`
    - Tipo: DATE
    - Obrigatorio: Sim
    - Descricao: Data em que a transacao foi realizada
  - `hora_inicial_transacao`
    - Tipo: VARCHAR(8)
    - Obrigatorio: Sim
    - Descricao: Hora em que a transacao foi realizada (formato HH:mm:ss)
  - `data_prevista_pagamento`
    - Tipo: DATE
    - Obrigatorio: Sim
    - Descricao: Data prevista para credito/recebimento do valor
  - `valor_liquido_transacao`
    - Tipo: NUMERIC(12,2)
    - Obrigatorio: Sim
    - Descricao: Valor liquido da transacao (apos deducao de taxas)
  - `valor_original_transacao`
    - Tipo: NUMERIC(12,2)
    - Obrigatorio: Sim
    - Descricao: Valor original da transacao
  - `maquina_id`
    - Tipo: UUID (FK -> eventos.maquina)
    - Obrigatorio: Nao
    - Descricao: Referencia a maquina associada a transacao. Null se nao houver `numeroSerieLeitor`
    - Restricoes: FK com ON DELETE SET NULL
  - `congregacao_id`
    - Tipo: UUID (FK -> eventos.congregacao)
    - Obrigatorio: Nao
    - Descricao: Congregacao herdada da maquina no momento da insercao
  - `departamento_id`
    - Tipo: UUID (FK -> eventos.departamento)
    - Obrigatorio: Nao
    - Descricao: Departamento herdado da maquina no momento da insercao
  - `created_at`
    - Tipo: TIMESTAMP WITH TIME ZONE
    - Obrigatorio: Sim (preenchido com `NOW()` no INSERT nativo)
    - Descricao: Data e hora de criacao do registro
  - `creation_user_id`
    - Tipo: VARCHAR(255)
    - Obrigatorio: Sim
    - Descricao: Valor fixo: `"BOT"` neste fluxo
  - `updated_at`
    - Tipo: TIMESTAMP WITH TIME ZONE
    - Obrigatorio: Nao
    - Descricao: Data e hora da ultima atualizacao
  - `mod_user_id`
    - Tipo: VARCHAR(255)
    - Obrigatorio: Nao
  - **Constraints:**
    - `uk_movimento_pagbank_soberano`: UNIQUE (codigo_transacao, tipo_evento, parcela) - constraint principal de deduplicacao
    - `idx_lancamento_numero_serie_leitor`: INDEX parcial em numero_serie_leitor (WHERE NOT NULL AND != '')
    - `idx_lancamento_congregacao_id`: INDEX em congregacao_id
    - `idx_lancamento_departamento_id`: INDEX em departamento_id

#### Entidade `MaquinaEntity`

- **Tabela / Recurso:** `eventos.maquina`
- **Descricao geral:** Terminal POS / maquina de cartao.

- **Campos principais:**
  - `id`
    - Tipo: UUID (PK)
    - Obrigatorio: Sim
  - `numero_serie_leitor`
    - Tipo: VARCHAR(20)
    - Obrigatorio: Sim
    - Descricao: Numero de serie unico da maquina
    - Restricoes: UNIQUE
  - `congregacao_id`
    - Tipo: UUID (FK -> eventos.congregacao)
    - Obrigatorio: Nao
    - Descricao: Congregacao vinculada a maquina. Null quando criada automaticamente neste fluxo
  - `banco_id`
    - Tipo: UUID (FK -> eventos.banco)
    - Obrigatorio: Sim (NOT NULL no schema)
    - Descricao: Banco/adquirente associado. Sempre PagBank ("290") neste fluxo
  - `departamento_id`
    - Tipo: UUID (FK -> eventos.departamento)
    - Obrigatorio: Nao
    - Descricao: Departamento vinculado. Null quando criada automaticamente neste fluxo
  - `ativo`
    - Tipo: BOOLEAN
    - Obrigatorio: Nao
    - Descricao: Indica se a maquina esta ativa. Default: true
  - `version`
    - Tipo: BIGINT
    - Obrigatorio: Nao
    - Descricao: Campo para controle de concorrencia otimista (JPA `@Version`). Default: 0
  - `created_at`, `updated_at`, `creation_user_id`, `mod_user_id`
    - Campos de auditoria herdados de `BaseAuditEntity`
  - **Constraints:**
    - UNIQUE em `numero_serie_leitor`
    - FK `congregacao_id` -> `eventos.congregacao(id)` ON DELETE CASCADE
    - FK `banco_id` -> `eventos.banco(id)` ON DELETE CASCADE
    - INDEX parcial em `numero_serie_leitor` (WHERE NOT NULL AND != '')

#### Entidade `BancoEntity`

- **Tabela / Recurso:** `eventos.banco`
- **Descricao geral:** Banco ou adquirente de pagamentos.

- **Campos principais:**
  - `id`
    - Tipo: UUID (PK)
    - Obrigatorio: Sim
    - PagBank: `a1b2c3d4-e5f6-7890-1234-567890abcdef` (fixo via migration)
  - `nome`
    - Tipo: VARCHAR(200)
    - Obrigatorio: Nao
    - PagBank: "PagBank"
  - `codigo`
    - Tipo: VARCHAR(10)
    - Obrigatorio: Sim
    - Restricoes: UNIQUE
    - PagBank: "290"
  - `endereco_completo`
    - Tipo: TEXT
    - Obrigatorio: Sim
  - `tipo_integracao`
    - Tipo: VARCHAR(20)
    - Obrigatorio: Sim
    - PagBank: "API"
  - `ativo`
    - Tipo: BOOLEAN
    - Default: true
  - **Observacao:** NAO possui campos de auditoria (herda apenas `BaseEntity` com `id`).

#### Entidade `CongregacaoEntity`

- **Tabela / Recurso:** `eventos.congregacao`
- **Descricao geral:** Unidade organizacional.

- **Campos principais relevantes para este fluxo:**
  - `id` - Tipo: UUID (PK)
  - `tenant_id` - Tipo: UUID (FK -> core.tenants). Obrigatorio: Sim
  - `setorial_id` - Tipo: UUID (FK auto-referencia -> eventos.congregacao). Obrigatorio: Nao
  - `nome` - Tipo: VARCHAR(255)
  - `ativo` - Tipo: BOOLEAN
  - **Observacao:** Neste fluxo, apenas o `id` e o `nome` sao relevantes (usados para vincular lancamentos).

#### Entidade `DepartamentoEntity`

- **Tabela / Recurso:** `eventos.departamento`
- **Descricao geral:** Departamento organizacional.

- **Campos principais relevantes para este fluxo:**
  - `id` - Tipo: UUID (PK)
  - `tenant_id` - Tipo: UUID (FK -> core.tenants). Obrigatorio: Sim
  - `nome` - Tipo: VARCHAR(255). Obrigatorio: Sim
  - `ativo` - Tipo: BOOLEAN. Obrigatorio: Sim
  - **Observacao:** Neste fluxo, apenas o `id` e o `nome` sao relevantes.

---

## 5. Operacoes de CRUD

### 5.1. Create (Criacao)

**MovimentoApiEntity:**
- **O que e criado:** Registro representando uma resposta da API PagBank (por dia e pagina).
- **Quando ocorre:** Ao receber e deserializar com sucesso a resposta da API PagBank. Cada pagina de resposta gera um registro separado.
- **Entradas obrigatorias:** payload (criptografado), status, pagina, totalElementos, totalPaginas, dataLeitura, creationUserId="CRON".
- **Validacoes na criacao:**
  - Constraint UNIQUE `(data_leitura, pagina)` - se ja existir registro para aquele dia+pagina, `DataIntegrityViolationException` e lancada.
- **Comportamento em caso de erro:** A excecao e propagada e o processamento do dia inteiro falha.

**LancamentoEntity:**
- **O que e criado:** Registro representando uma transacao financeira individual.
- **Quando ocorre:** Durante o processamento transacional, para cada transacao extraida do dia.
- **Entradas obrigatorias:** Todos os campos obrigatorios da transacao PagBank + maquina_id (se disponivel), congregacao_id, departamento_id.
- **Validacoes na criacao:**
  - INSERT nativo com `ON CONFLICT (codigo_transacao, tipo_evento, parcela) DO NOTHING` - duplicatas sao silenciosamente ignoradas.
  - O ID e gerado via `gen_random_uuid()` no PostgreSQL.
  - `created_at` e preenchido com `NOW()`.
- **Comportamento em caso de erro:** Se qualquer INSERT falhar (por motivo diferente de duplicata), toda a transacao do dia e revertida (rollback).

**MaquinaEntity:**
- **O que e criado:** Registro representando uma maquina de cartao/terminal POS.
- **Quando ocorre:** Quando uma transacao referencia um `numeroSerieLeitor` que nao existe no banco.
- **Entradas obrigatorias:** numeroSerieLeitor, banco=PagBank, ativo=true, creationUserId="BOT".
- **Validacoes na criacao:**
  - UNIQUE em `numero_serie_leitor` - tratado via retry em caso de conflito de concorrencia.
- **Comportamento em caso de erro:** Retry automatico com ate 5 tentativas.

### 5.2. Read (Consulta/Leitura)

- **`MovimentoApiRepository.findFirstByOrderByDataLeituraDesc()`**
  - O que consulta: Ultimo registro de `movimento_api` por data de leitura.
  - Quando: Inicio do fluxo, para determinar o ponto de partida.
  - Retorno: `Optional<MovimentoApiEntity>`.

- **`MovimentoApiRepository.findByDataLeituraAndPagina(Date, int)`**
  - O que consulta: Registro especifico de `movimento_api` por data e pagina.
  - Quando: Antes do processamento transacional de lancamentos.
  - Retorno: `Optional<MovimentoApiEntity>`.

- **`MaquinaRepository.findByNumeroSerieLeitorIn(Set<String>)`**
  - O que consulta: Maquinas existentes pelos numeros de serie.
  - Quando: Antes de criar maquinas novas (busca em lote).
  - Retorno: `List<MaquinaEntity>`.
  - Cache: `@Cacheable(value = "findByNumeroSerieLeitorIn")`.

- **`MaquinaRepository.findByNumeroSerieLeitor(String)`**
  - O que consulta: Maquina individual por numero de serie.
  - Quando: No metodo `findOrCreateMaquina()` (uso individual).
  - Retorno: `Optional<MaquinaEntity>`.
  - Cache: `@Cacheable(value = "findByNumeroSerieLeitor")`.
  - Lock: `@Lock(LockModeType.PESSIMISTIC_WRITE)`.

- **`BancoRepository.findByCodigo("290")`**
  - O que consulta: Banco PagBank pelo codigo.
  - Quando: Inicializacao da aplicacao (bean `pagBank`).
  - Retorno: `Optional<BancoEntity>`.

### 5.3. Update (Atualizacao)

- **O que e atualizado:** `MovimentoApiEntity`.
- **Quando ocorre:** Apos processamento dos lancamentos de um dia (no bloco `finally`).
- **Campos atualizaveis:**
  - `status`: de `RECEBIDO` para `PROCESSADA` (sucesso) ou `ERRO_PROCESSAMENTO` (falha).
  - `updatedAt`: atualizado automaticamente (`@UpdateTimestamp` ou manualmente via `setUpdatedAt(ZonedDateTime.now())`).
- **Restricoes:** Nenhuma outra entidade e atualizada neste fluxo.

### 5.4. Delete (Exclusao)

- **Nao ha exclusao neste fluxo.**
- **Nota historica:** Existe uma migration (`V20251109095000__DML-delete-lancamentos-inferior11_25.sql`) que fez limpeza pontual de dados anteriores a 2025-11-01, deletando lancamentos e movimentos antigos. Isso nao faz parte do fluxo de extracao.

---

## 6. Regras de Negocio

### Regra RN-001 - Determinacao do Range de Datas

- **Descricao:**
  O sistema determina automaticamente quais dias precisam ser processados, partindo do ultimo dia ja registrado em `movimento_api` ate D-1 (dia anterior ao atual). Essa logica garante que a extracao seja incremental e nao reprocesse dias ja extraidos.

- **Contexto de aplicacao:**
  Inicio do metodo `extrairMovimentos()`, antes de qualquer chamada a API externa.

- **Condicoes de entrada:**
  - Existencia (ou nao) de registros na tabela `movimento_api`.

- **Comportamento esperado:**
  - Se existem registros: usa o campo `createdAt` (ZonedDateTime) do registro com maior `data_leitura` como ponto de partida, truncado para o inicio do dia.
  - Se NAO existem registros (ou `createdAt` e null): usa a data configurada em `eventos.bancos.pagbank.inicio` (default: `2025-01-01`), convertida para ZonedDateTime em UTC.
  - A data final e sempre D-1 (`ZonedDateTime.now().minusDays(1)`) truncada para o inicio do dia.
  - Se a ultima data processada ja e posterior a D-1: nenhum processamento e realizado, apenas loga informacao.
  - A lista de datas gerada inclui tanto a data inicial quanto a data final (range inclusivo).
  - A pagina do ultimo evento e utilizada como pagina inicial para o primeiro dia da lista. Se `pagina < 1`, usa 1.

- **Dependencias:**
  - Configuracao `eventos.bancos.pagbank.inicio` (variavel de ambiente `PAGESEGURO_DATA_INICIO`).
  - Tabela `eventos.movimento_api`.

### Regra RN-002 - Limite de Concorrencia no Processamento

- **Descricao:**
  O processamento de dias e feito em paralelo utilizando virtual threads, porem limitado a no maximo 3 dias processados simultaneamente. Isso protege tanto a API PagBank quanto o banco de dados de sobrecarga.

- **Contexto de aplicacao:**
  Metodo `extrairMovimentos()`, durante a submissao de tarefas assincronas.

- **Condicoes de entrada:**
  - Lista de datas a processar.

- **Comportamento esperado:**
  - Usa `Executors.newVirtualThreadPerTaskExecutor()` para criar threads virtuais (Java 21).
  - Um `Semaphore(3)` controla que no maximo 3 dias sejam processados ao mesmo tempo.
  - Cada tarefa adquire o semaforo antes de processar e libera no `finally`.
  - `CompletableFuture.allOf().join()` aguarda todas as tarefas concluirem.
  - Shutdown do executor com timeout de 60 segundos; se nao encerrar, usa `shutdownNow()`.

### Regra RN-003 - Validacao do Header "validado"

- **Descricao:**
  A resposta da API PagBank so e processada se o header HTTP `validado` retornar `"true"`. Esta e uma validacao imposta pelo PagBank para indicar que os dados do dia estao consolidados e prontos para consumo. Se o header nao estiver presente ou tiver qualquer outro valor, a resposta e ignorada silenciosamente.

- **Contexto de aplicacao:**
  Metodos `processarDia()` e `paginar()`, apos cada chamada a API PagBank.

- **Condicoes de entrada:**
  - Header HTTP `validado` presente no `ResponseEntity` retornado pelo Feign Client.

- **Comportamento esperado:**
  - Se o valor do header for `"true"` (case-insensitive, via `StringToBooleanConverter`): processa a resposta normalmente.
  - Se o valor for qualquer outro, `null` ou o header estiver ausente: ignora a resposta por completo. Nao salva nada, nao gera erro, nao registra log de erro.

### Regra RN-004 - Criptografia do Payload

- **Descricao:**
  Todo payload JSON recebido da API PagBank e criptografado antes de ser armazenado no banco de dados, para protecao de dados financeiros sensiveis. A descriptografia e usada apenas durante reprocessamento.

- **Contexto de aplicacao:**
  Metodo `salvarMovimentoApi()` para criptografar; `MovimentoApiResponseConversor.decryptMovimentoApiResponse()` para descriptografar durante reprocessamento.

- **Condicoes de entrada:**
  - Payload JSON (string) retornado pela API PagBank.

- **Comportamento esperado:**
  - Criptografia: AES-256/GCM/NoPadding com IV aleatorio de 12 bytes.
  - Formato armazenado: `Base64(IV[12 bytes] + ciphertext + GCM_TAG[16 bytes])`.
  - Chave: AES-256 derivada de Base64 decode da configuracao `eventos.bancos.encryption.key`.
  - A descriptografia extrai o IV dos primeiros 12 bytes do Base64-decoded, e usa o restante como ciphertext.
  - Se o JSON estiver vazio, `encrypt()` retorna string vazia sem criptografar.

- **Dependencias:**
  - `JsonEncryptorDecryptor` (algoritmo AES/GCM).
  - `EventosSecretKey` (decodificacao da chave Base64 -> SecretKey AES-256).
  - `CryptoConfig` (configuracao dos beans).
  - Configuracao `eventos.bancos.encryption.key`.

### Regra RN-005 - Deduplicacao de Lancamentos

- **Descricao:**
  Lancamentos duplicados sao ignorados silenciosamente durante a insercao, garantindo idempotencia do fluxo. A deduplicacao e feita via constraint UNIQUE no banco de dados combinada com `ON CONFLICT DO NOTHING`.

- **Contexto de aplicacao:**
  Metodo `salvarIgnorandoDuplicata()` no `LancamentoRepository`.

- **Condicoes de entrada:**
  - Lancamento com `codigo_transacao`, `tipo_evento` e `parcela` identicos a um registro ja existente.

- **Comportamento esperado:**
  - A query nativa usa `INSERT ... ON CONFLICT (codigo_transacao, tipo_evento, parcela) DO NOTHING`.
  - Se ja existe um lancamento com a mesma combinacao dessas tres colunas, o INSERT e simplesmente ignorado.
  - Nenhuma excecao e lancada. Nenhum log e gerado.
  - Isso permite que o mesmo dia seja re-extraido sem gerar duplicatas.

- **Dependencias:**
  - Constraint `uk_movimento_pagbank_soberano` na tabela `eventos.lancamento`.

### Regra RN-006 - Criacao Automatica de Maquinas

- **Descricao:**
  Quando uma transacao extraida referencia um `numeroSerieLeitor` que nao existe no cadastro de maquinas, o sistema cria automaticamente uma nova maquina vinculada ao PagBank. A congregacao e departamento ficam null (serao vinculados posteriormente em outro fluxo).

- **Contexto de aplicacao:**
  `MaquinaManagementService.findOrCreateMaquinasBatch()`, chamado durante o processamento transacional.

- **Condicoes de entrada:**
  - O `numeroSerieLeitor` nao e nulo nem vazio (filtrado previamente).
  - Nao existe `MaquinaEntity` com esse `numeroSerieLeitor` no banco.

- **Comportamento esperado:**
  - Busca em lote (`findByNumeroSerieLeitorIn`) todas as maquinas existentes.
  - Para cada serie nao encontrada, cria `MaquinaEntity` com:
    - `numeroSerieLeitor`: valor vindo da transacao.
    - `banco`: referencia ao bean PagBank (BancoEntity com codigo "290").
    - `ativo`: `true`.
    - `creationUserId`: `"BOT"`.
    - `congregacao`: `null`.
    - `departamento`: `null`.
  - Salva em lote via `maquinaRepository.saveAll()`.
  - Retorna um mapa `String -> MaquinaEntity` (numeroSerieLeitor -> maquina).

- **Dependencias:**
  - Bean `pagBank` (BancoEntity) carregado na inicializacao.
  - `MaquinaRepository`.

### Regra RN-007 - Heranca de Congregacao e Departamento

- **Descricao:**
  O `congregacao_id` e `departamento_id` de cada lancamento sao herdados da maquina associada no momento da insercao. Esses campos NAO vem da API PagBank; sao enriquecidos pelo sistema com base no cadastro de maquinas.

- **Contexto de aplicacao:**
  Metodo `processarEmTransacao()`, ao inserir cada lancamento.

- **Condicoes de entrada:**
  - Lancamento com `numeroSerieLeitor`.
  - MaquinaEntity correspondente (pode ter congregacao e departamento vinculados ou nao).

- **Comportamento esperado:**
  - Se a maquina existe e tem `congregacao`: lancamento herda `maquina.getCongregacao().getId()`.
  - Se a maquina existe e tem `departamento`: lancamento herda `maquina.getDepartamento().getId()`.
  - Se a maquina nao tem congregacao/departamento (null): campos ficam null no lancamento.
  - Se o lancamento nao tem `numeroSerieLeitor`: `maquina_id`, `congregacao_id` e `departamento_id` ficam todos null.

### Regra RN-008 - Controle de Status de Processamento

- **Descricao:**
  O status do `MovimentoApiEntity` controla o ciclo de vida do processamento de cada resposta da API e impede reprocessamento indevido. Funciona como maquina de estados.

- **Contexto de aplicacao:**
  Todo o fluxo de processamento.

- **Condicoes de entrada:**
  - Status atual do `MovimentoApiEntity`.

- **Comportamento esperado (maquina de estados):**
  - `RECEBIDO` -> Estado inicial. Atribuido ao salvar a resposta da API com sucesso.
  - `RECEBIDO` -> `PROCESSADA` -> Transicao apos processamento bem-sucedido dos lancamentos.
  - `RECEBIDO` -> `ERRO_PROCESSAMENTO` -> Transicao em caso de erro durante processamento dos lancamentos.
  - `(qualquer)` -> `ERRO_PAYLOAD` -> Atribuido quando o JSON nao pode ser deserializado.
  - `(qualquer)` -> `ERRO_COMUNICACAO` -> Atribuido quando falha a paginacao (chamada HTTP falhou).
  - `(qualquer)` -> `ERRO_INTERNO` -> Atribuido para outros erros inesperados.
  - **Regra de guarda:** O processamento de lancamentos SO ocorre se o status do `MovimentoApiEntity` for exatamente `RECEBIDO`. Qualquer outro status impede o processamento.

### Regra RN-009 - Mapeamento de Enums via Codigo

- **Descricao:**
  Os campos de tipo de evento, meio de captura e meio de pagamento vem da API PagBank como codigos numericos (strings) e sao convertidos para enums Java com descricoes legíveis. Se o codigo nao for reconhecido, um valor padrao e utilizado (nunca lanca excecao).

- **Contexto de aplicacao:**
  Deserializacao do JSON via Jackson (`@JsonCreator` nos enums).

- **Condicoes de entrada:**
  - Campo string do JSON retornado pela API PagBank.

- **Mapeamentos completos:**

  **TipoEventoEnum (codigo PagBank -> enum -> descricao):**

  | Codigo | Enum | Descricao |
  |--------|------|-----------|
  | "1" | VENDA_OU_PAGAMENTO | Venda ou Pagamento |
  | "2" | AJUSTE_CREDITO | Ajuste a Credito |
  | "3" | AJUSTE_DEBITO | Ajuste a Debito |
  | "4" | TRANSFERENCIA_OUTROS_BANCOS | Transferencia para outros Bancos |
  | "5" | CHARGEBACK | Chargeback |
  | "" (vazio) | CANCELAMENTO | Cancelamento |
  | "7" | SALDO_INICIAL | Saldo Inicial |
  | "8" | SALDO_FINAL | Saldo Final |
  | "9" | ABERTURA_DISPUTA | Abertura Disputa |
  | "10" | ENCERRAMENTO_DISPUTA | Encerramento Disputa |
  | "11" | ABERTURA_PRE_CHARGEBACK | Abertura Pre-Chargeback |
  | "12" | ENCERRAMENTO_PRE_CHARGEBACK | Encerramento Pre-Chargeback |
  | "16" | RENDIMENTO_CONTA | Rendimento da Conta |
  | "18" | SAQUE_CANCELADO | Saque Cancelado |
  | "19" | DEBITO_DIVISAO_PAGAMENTO | Debito Divisao de Pagamento |
  | "20" | CREDITO_DIVISAO_PAGAMENTO | Credito Divisao de Pagamento |
  | "21" | AQUISICAO_ENVIO_FACIL | Aquisicao do Envio Facil |
  | "22" | APORTE_ENVIO_FACIL | Aporte para Aquisicao do Envio Facil |
  | "23" | RECUPERACAO_EMPRESTIMO | Recuperacao de Emprestimo |
  | "24" | ARRECADACAO_EMPRESTIMO | Arrecadacao de Emprestimo |
  | "25" | SAQUE_ESTORNADO | Saque Estornado |
  | "26" | DESCONTO_TAXA_CANCELAMENTO | Desconto de Taxa por Cancelamento |
  | "27" | DESCONTO_TAXA_CHARGEBACK | Desconto de Taxa por Chargeback |
  | "28" | DEVOLUCAO_TAXA_REVERSAO_CHARGEBACK | Devolucao de Taxa por Reversao de Chargeback |
  | "29" | BLOQUEIO_CUSTODIA | Bloqueio por Custodia |
  | "30" | DESBLOQUEIO_CUSTODIA | Desbloqueio por Custodia |
  | "31" | PRAZO_ADIADO_ANALISE | Prazo de Recebimento Adiado para Analise |
  | "32" | PRAZO_LIBERADO | Prazo de Recebimento Liberado |
  | null/desconhecido | DESCONHECIDO | Desconhecido (codigo "00") |

  **MeioCapturaEnum (codigo PagBank -> enum -> descricao):**

  | Codigo | Enum | Descricao |
  |--------|------|-----------|
  | "1" | CHIP | Chip |
  | "2" | TARJA | Tarja |
  | "3" | NAO_PRESENCIAL | Nao Presencial |
  | "4" | TEF | TEF |
  | "5" | QR_CODE | QR CODE |
  | null/desconhecido | OUTRO | Outro (codigo "0") |

  **MeioPagamentoEnum (codigo PagBank -> enum -> descricao):**

  | Codigo | Enum | Descricao |
  |--------|------|-----------|
  | "0" | OPERACAO_CONTA_ADQUIRENCIA | Operacao Conta adquirencia PagBank |
  | "1" | DEBITO_ONLINE | Debito Online |
  | "2" | BOLETO | Boleto |
  | "3" | CARTAO_CREDITO | Cartao de Credito |
  | "4" | SALDO | Saldo |
  | "7" | DEPOSITO_CONTA | Deposito em Conta |
  | "8" | CARTAO_DEBITO | Cartao de Debito |
  | "9" | DEBITO_AUTOMATICO | Debito Automatico em Conta |
  | "10" | VOUCHER | Voucher |
  | "11" | PIX | Pix |
  | "12" | CARNE_CREDITO | Carne Credito |
  | "13" | CARNE_DEBITO | Carne Debito |
  | "14" | CARTAO_CREDITO_PRE_PAGO | Cartao de Credito Pre-Pago |
  | "15" | CARTAO_DEBITO_PRE_PAGO | Cartao de Debito Pre-Pago |
  | "16" | CARNE_CREDITO_PRE_PAGO | Carne Credito Pre-Pago |
  | "17" | CARNE_DEBITO_PRE_PAGO | Carne Debito Pre-Pago |
  | null/desconhecido | OUTRO | Outro (codigo "99") |

- **Comportamento esperado:**
  - Se o valor recebido corresponde a um `codigo` de enum: retorna o enum correspondente.
  - Se o valor recebido corresponde ao `name()` de um enum (ex: "CHIP"): tambem retorna o enum correspondente.
  - Se o valor e `null` ou nao reconhecido: retorna o valor padrao (`DESCONHECIDO`, `OUTRO`, `OUTRO` respectivamente). Nunca lanca excecao.

### Regra RN-010 - Persistencia Atomica do Dia

- **Descricao:**
  O processamento dos lancamentos de um dia e feito em uma unica transacao de banco de dados. Se qualquer lancamento falhar (exceto duplicatas que sao ignoradas), todos os lancamentos daquele dia sao revertidos. Porem, o `MovimentoApiEntity` continua salvo (com status de erro), permitindo reprocessamento futuro.

- **Contexto de aplicacao:**
  `ProcessamentoLancamentoTransactional.processarEmTransacao()`.

- **Comportamento esperado:**
  - Sucesso: todos os lancamentos do dia sao inseridos, status = `PROCESSADA`.
  - Falha: rollback de todos os lancamentos daquele dia, status = `ERRO_PROCESSAMENTO`.
  - O `MovimentoApiEntity` e SEMPRE atualizado no bloco `finally` (salvo em qualquer caso).
  - A criacao de maquinas ocorre em transacao SEPARADA (`REQUIRES_NEW`), portanto nao e afetada pelo rollback dos lancamentos.

### Regra RN-011 - Unicidade de Movimento por Dia e Pagina

- **Descricao:**
  Existe uma constraint UNIQUE em `(data_leitura, pagina)` na tabela `movimento_api`, garantindo que nao haja registros duplicados para a mesma data e pagina.

- **Contexto de aplicacao:**
  `salvarMovimentoApi()`.

- **Comportamento esperado:**
  - Se tentar inserir um `MovimentoApiEntity` com `data_leitura` e `pagina` ja existentes, `DataIntegrityViolationException` e lancada.
  - A excecao e propagada, causando falha no processamento daquele dia.
  - Isso impede que a mesma extracao seja salva mais de uma vez para o mesmo dia/pagina.

### Regra RN-012 - Identificacao do Usuario de Criacao

- **Descricao:**
  Os registros criados por este fluxo automatico sao identificados com usuarios fixos, permitindo auditoria e diferenciacao entre registros manuais e automaticos.

- **Contexto de aplicacao:**
  Criacao de `MovimentoApiEntity`, `LancamentoEntity` e `MaquinaEntity`.

- **Comportamento esperado:**
  - `MovimentoApiEntity.creationUserId` = `"CRON"` - indica que o registro foi criado por execucao agendada.
  - `LancamentoEntity.creationUserId` = `"BOT"` - indica que a transacao foi inserida automaticamente.
  - `MaquinaEntity.creationUserId` = `"BOT"` - indica que a maquina foi criada automaticamente.

### Regra RN-013 - Banco PagBank Obrigatorio

- **Descricao:**
  O banco PagBank (codigo "290") deve existir na tabela `eventos.banco` para que a aplicacao funcione. E carregado como bean Spring (`pagBank`) na inicializacao e injetado no `MaquinaManagementService`.

- **Contexto de aplicacao:**
  `PagBankConfig` (classe de configuracao Spring) - executada na inicializacao.

- **Comportamento esperado:**
  - Se o PagBank existir no banco: carrega normalmente como bean.
  - Se NAO existir: a aplicacao lanca excecao com mensagem descritiva e NAO inicia.

- **Pre-requisito:**
  - Migration `V20250912231111__schema_initialization.sql` insere o PagBank com ID fixo `a1b2c3d4-e5f6-7890-1234-567890abcdef`, codigo `"290"`, nome `"PagBank"`, tipo_integracao `"API"`.

### Regra RN-014 - Armazenamento de Enums como Nome (nao codigo)

- **Descricao:**
  Os enums sao armazenados no banco de dados pelo NOME do enum Java (ex: `"VENDA_OU_PAGAMENTO"`, `"CHIP"`, `"PIX"`), e NAO pelo codigo numerico da API PagBank (ex: `"1"`, `"3"`, `"11"`).

- **Contexto de aplicacao:**
  Insercao via `salvarIgnorandoDuplicata()` - usa `.name()` em cada enum: `tipoEvento.name()`, `meioCaptura.name()`, `meioPagamento.name()`.

- **Comportamento esperado:**
  - Na deserializacao do JSON: codigo numerico -> enum Java (via `@JsonCreator`).
  - Na gravacao no banco: enum Java -> `.name()` (nome do enum) (via cast para VARCHAR na query nativa).
  - As colunas `tipo_evento`, `meio_captura` e `meio_pagamento` da tabela `lancamento` armazenam VARCHAR com o nome do enum Java, nao o codigo PagBank.

- **Impacto para o novo sistema:**
  - O banco de dados existente ja contem dados com nomes de enum (ex: `"VENDA_OU_PAGAMENTO"`).
  - O novo sistema DEVE usar os mesmos nomes de enum para manter compatibilidade.

### Regra RN-015 - Status de Paginas Adicionais em Dias Paginados

- **Descricao:**
  Em dias com multiplas paginas de resultados, APENAS o `MovimentoApiEntity` da pagina 1 tem seu status atualizado para `PROCESSADA` ou `ERRO_PROCESSAMENTO`. Os registros das paginas 2, 3, etc. permanecem com status `RECEBIDO` indefinidamente.

- **Contexto de aplicacao:**
  `LancamentoAdapter.processarMovimentoApiResponse()` - busca o `MovimentoApiEntity` usando `findByDataLeituraAndPagina(dataLeitura, pagina)` com `pagina=1` fixo (definido no `MovimentoApiResponseEvent`).

- **Comportamento esperado:**
  - Todas as transacoes de todas as paginas sao combinadas e processadas em uma unica operacao.
  - O status final (PROCESSADA ou erro) e atualizado apenas no registro da pagina 1.
  - Registros das paginas 2+ permanecem com status `RECEBIDO` mesmo apos processamento completo.

- **Impacto para o novo sistema:**
  - Considerar se este comportamento deve ser mantido ou se todas as paginas devem ter o status atualizado.

### Regra RN-016 - Uso da Pagina do Ultimo Evento para Todos os Dias

- **Descricao:**
  A pagina do ultimo evento processado e utilizada como pagina inicial para TODOS os dias do range, nao apenas para o primeiro dia. Isso e um comportamento projetado para retomada de processamento incompleto, mas pode causar perda de dados se o ultimo registro tiver pagina > 1.

- **Contexto de aplicacao:**
  Metodo `extrairMovimentos()`, ao gerar as tarefas de processamento por dia.

- **Comportamento esperado:**
  - A variavel `pagina` e extraida do ultimo `MovimentoApiEntity` (por `data_leitura DESC`).
  - Se `pagina < 1`, usa 1 como default.
  - A MESMA `pagina` e passada para `processarDia(data, pagina)` de todos os dias do range.
  - No fluxo normal (proximo dia apos processamento completo), o esperado e `pagina = 1`.
  - Se o ultimo registro ficou em pagina > 1 (dia incompleto), o processamento do proximo dia comecaria da pagina 2, perdendo a pagina 1.

- **Impacto para o novo sistema:**
  - Considerar tratar a pagina de retomada apenas para o primeiro dia do range e usar pagina 1 para os dias subsequentes.

### Regra RN-017 - Comportamento com JSON Vazio

- **Descricao:**
  Se a API PagBank retornar um JSON vazio (ou string vazia), o sistema nao lanca excecao. Em vez disso, cria um `MovimentoApiResponse` vazio com lista de transacoes vazia e paginacao padrao.

- **Contexto de aplicacao:**
  `MovimentoApiResponseConversor.getMovimentoApiResponse()`.

- **Comportamento esperado:**
  - Se o JSON for nulo ou vazio (`ObjectUtils.isEmpty(json)`): retorna `MovimentoApiResponse.newBuilder().build()`, que contem `detalhes` como `ArrayList` vazio e `pagination` como `Pagination(1, 1, 0)`.
  - Isso permite que o fluxo continue sem gerar `JsonProcessingException`.
  - O dia sera marcado como `PROCESSADA` sem inserir nenhum lancamento.

### Regra RN-018 - Resiliencia na Criacao de Maquinas

- **Descricao:**
  A criacao de maquinas ocorre em transacao separada (REQUIRES_NEW) com retry automatico para lidar com conflitos de concorrencia. Isso garante que maquinas criadas nao sejam afetadas pelo rollback de lancamentos.

- **Contexto de aplicacao:**
  `MaquinaManagementService.findOrCreateMaquinasBatch()`.

- **Comportamento esperado:**
  - Transacao: `Propagation.REQUIRES_NEW` (independente da transacao principal).
  - Retry: ate 5 tentativas para `ObjectOptimisticLockingFailureException` e `DataIntegrityViolationException`.
  - Backoff: exponencial, iniciando em 200ms (200ms, 400ms, 800ms, 1600ms, 3200ms).
  - Cache: resultados de busca de maquinas sao cacheaveis (`@Cacheable`).
  - Lock: busca individual (`findByNumeroSerieLeitor`) usa `@Lock(PESSIMISTIC_WRITE)` para serializar acessos concorrentes. A busca em lote (`findByNumeroSerieLeitorIn`) NAO usa lock pessimista, apenas cache.

### Regra RN-020 - Resposta do Endpoint de Extracao

- **Descricao:**
  O endpoint de extracao retorna HTTP 202 Accepted com o tempo total de execucao em segundos no body da resposta.

- **Contexto de aplicacao:**
  `MovimentoApiController.extrairTransacoes()`.

- **Comportamento esperado:**
  - Mede o tempo de execucao com `Instant.now()` antes e depois.
  - Retorna `ResponseEntity.accepted()` (HTTP 202).
  - Body: string no formato `"Extracao finalizada - {segundos}"` (ex: `"Extracao finalizada - 45"`).
  - A requisicao e sincrona (nao retorna imediatamente; aguarda todo o processamento concluir).

### Regra RN-021 - Reprocessamento via Controller Separado

- **Descricao:**
  O reprocessamento de um movimento ja salvo e feito via um controller diferente (`MovimentoController`), com rota e autenticacao distintas do endpoint de extracao.

- **Contexto de aplicacao:**
  `MovimentoController` (NAO `MovimentoApiController`).

- **Comportamento esperado:**
  - Rota: `/movimentos/reprocessar/{id}` (sem prefixo `/api/`).
  - Autenticacao: sessao web (Spring Security form login), com roles: FISCAL, ADMIN ou ADMIN_MATRIZ.
  - O metodo busca o `MovimentoApiEntity` pelo UUID, descriptografa o payload, recria a lista de lancamentos e re-executa o processamento.
  - Resposta: HTTP 200 com body `"Movimento {id} enviado para ser processado."`.
  - O fluxo de reprocessamento utiliza `MovimentoApiAdapter.reprocessar()` que chama `converterLancamento()` da mesma forma que o fluxo de extracao.

### Regra RN-022 - Seguranca Desabilitada em Ambiente de Desenvolvimento

- **Descricao:**
  Quando a configuracao `eventos.app.security.enabled` esta como `false`, toda a seguranca (incluindo o `ApiKeyAuthFilter`) e desabilitada. Qualquer requisicao a `/api/**` e permitida sem autenticacao.

- **Contexto de aplicacao:**
  `WebSecurityConfig.securityFilterChain()`.

- **Comportamento esperado:**
  - Se `securityEnabled = false`: CSRF desabilitado, todas as requisicoes permitidas (`anyRequest().permitAll()`), sem filtro de API key.
  - Se `securityEnabled = true`: o `ApiKeyAuthFilter` e adicionado antes do `UsernamePasswordAuthenticationFilter`, e `/api/**` esta como `permitAll` no Spring Security (mas protegido pelo filtro de API key).

### Regra RN-019 - Resiliencia no Processamento Transacional

- **Descricao:**
  O processamento transacional de lancamentos possui retry automatico para falhas transientes de conexao com o banco de dados.

- **Contexto de aplicacao:**
  `ProcessamentoLancamentoTransactional.processarEmTransacao()`.

- **Comportamento esperado:**
  - Retry: ate 3 tentativas para `CannotCreateTransactionException` e `JDBCConnectionException`.
  - Backoff: exponencial, iniciando em 2000ms (2s, 4s).

---

## 7. Tratamento de Erros e Excecoes

- **Tipos de erros tratados:**
  - **Erro de deserializacao JSON** (`JsonProcessingException`) -> Salva `MovimentoApiEntity` com status `ERRO_PAYLOAD`. Lanca RuntimeException.
  - **Erro de integridade de dados** (`DataIntegrityViolationException`) -> Propaga excecao sem salvar (possivel duplicata em `movimento_api`).
  - **Erro de comunicacao HTTP** (falha Feign/paginacao) -> Salva `MovimentoApiEntity` com status `ERRO_COMUNICACAO`. Lanca RuntimeException.
  - **Erro de processamento de lancamentos** (excecao durante insercao) -> Status `ERRO_PROCESSAMENTO` no `MovimentoApiEntity`.
  - **Erro generico/interno** (qualquer outro) -> Salva `MovimentoApiEntity` com status `ERRO_INTERNO`. Lanca RuntimeException.
  - **Erro de conexao ao banco** (`CannotCreateTransactionException`, `JDBCConnectionException`) -> Retry automatico (3 tentativas).
  - **Erro de concorrencia otimista** (`ObjectOptimisticLockingFailureException`) -> Retry automatico (5 tentativas) na criacao de maquinas.
  - **Thread interrompida** (`InterruptedException`) -> Re-define flag de interrupcao (`Thread.currentThread().interrupt()`) e loga erro.

- **Comportamento do sistema em erros:**
  - Erro em um dia NAO interrompe processamento dos demais dias (isolamento por dia).
  - Erro em uma pagina adicional INTERROMPE processamento daquele dia inteiro.
  - O `MovimentoApiEntity` e SEMPRE salvo, mesmo em caso de erro (bloco `finally`).
  - A transacao de lancamentos e atomica (tudo ou nada por dia).
  - Maquinas sao criadas em transacao separada (`REQUIRES_NEW`), nao afetadas pelo rollback dos lancamentos.
  - O payload original criptografado fica salvo em `movimento_api`, permitindo reprocessamento futuro.

- **Mecanismos de resiliencia:**
  - Retry no processamento transacional: 3 tentativas, backoff exponencial 2s -> 4s.
  - Retry na criacao de maquinas: 5 tentativas, backoff exponencial 200ms -> 3.2s.
  - Semaforo para limitar concorrencia: max 3 dias simultaneos.
  - Virtual threads para processamento eficiente sem bloqueio de threads de SO.
  - Cache de maquinas (`@Cacheable`) para reduzir consultas repetitivas ao banco.
  - `ON CONFLICT DO NOTHING` para idempotencia na insercao de lancamentos.

---

## 8. Observacoes Adicionais

- **API PagBank EDI:**
  - URL base: `https://edi.api.pagbank.com.br`
  - Endpoint: `/movement/v3.00/transactional/{data}` (versao 3.00)
  - Autenticacao: HTTP Basic Auth (configurado via `FeignConfig` que cria `BasicAuthRequestInterceptor` usando username/password de `eventos.bancos.pagbank.pags.*`)
  - Formato de resposta: JSON com campos em snake_case
  - Header de validacao: `validado` (retornado pelo PagBank para indicar dados consolidados)

- **Estrutura do JSON da API PagBank:**
  O record `MovimentoApiResponse` mapeia o JSON com dois campos: `detalhes` (lista de transacoes) e `pagination` (metadados de paginacao). O campo `detalhes` do JSON e mapeado para `List<Lancamento>`. O Builder interno usa `lancamentos` como nome do metodo, mas o record expoe `detalhes()` como accessor. A paginacao e `Pagination(totalPages, page, totalElements)`.

- **Fallback para MovimentoApiEntity vazio:**
  Quando `buscarUltimoEventoProcessado()` nao encontra registros, usa `findFirstByOrderByDataLeituraDesc().orElse(new MovimentoApiEntity())`. O `MovimentoApiEntity` vazio tem `createdAt = null`, o que aciona o fallback para a data configurada em `pagbankInicio`.

- **Formato dos dados da API:**
  O JSON retornado pela API PagBank usa convencao snake_case para nomes de campos. O mapeamento e feito automaticamente via `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)` no record `Lancamento`. Campos desconhecidos sao ignorados via `@JsonIgnoreProperties(ignoreUnknown = true)`.

- **Endpoint secundario:**
  Alem do `/extrair`, existe `GET /api/movimentos/lacamentoDoDia?data=YYYY-MM-DD` para extrair movimentos de uma data especifica. Nota: ha um typo no nome do endpoint ("lacamento" ao inves de "lancamento"). Este endpoint chama `processarDia()` com a data fornecida e pagina 1, usando `ZoneId.systemDefault()` (ao inves de UTC).

- **Reprocessamento:**
  O metodo `reprocessar(UUID movimentoID)` permite reprocessar um movimento ja salvo: busca o `MovimentoApiEntity` pelo ID, descriptografa o payload, e re-executa o processamento de lancamentos. Util para recuperar de erros sem necessidade de re-consultar a API PagBank.

- **Configuracoes de infraestrutura:**
  - HikariCP: pool de conexoes com max 10, min 2, idle timeout 30s, max lifetime 1800s.
  - Virtual Threads: habilitadas globalmente (`spring.threads.virtual.enabled: true`).
  - Hibernate batch_size: 1 (sem batch de insercoes via JPA; lancamentos sao inseridos via query nativa individual).

- **Timezone:**
  - O processamento geral usa UTC para datas (`ZoneId.of("UTC")`).
  - O `DateTimeUtil.toDate()` usa `SimpleDateFormat("yyyy-MM-dd")` sem timezone explicito (usa timezone padrao da JVM).
  - O endpoint `/lacamentoDoDia` usa `ZoneId.systemDefault()` ao inves de UTC.

- **Resilience4j:**
  Configurado apenas em testes do financial-connectors (`max-attempts: 5, wait-duration: 7200s` para o PagSeguroClient). NAO ha configuracao de Resilience4j em producao. A resiliencia em producao depende exclusivamente do Spring Retry.

- **Profiles de teste:**
  `PagBankConfig` e `CryptoConfig` sao anotadas com `@Profile("!test")` - seus beans nao sao criados em perfil de teste. Em testes, esses beans precisam ser mockados ou sobrescritos (ex.: `BeanOverrides`).

- **Dependencias entre fluxos:**
  - Este fluxo depende do cadastro de bancos (PagBank deve existir).
  - Outros fluxos dependem deste: tela de lancamentos, repasses, relatorios - todos consomem os lancamentos criados por este fluxo.
  - A vinculacao maquina-congregacao e feita em outro fluxo (cadastro de maquinas), e afeta os lancamentos futuros (congregacao_id e departamento_id herdados).

---

## 9. Regras e Diretrizes para Implementacao no Novo Projeto

### 9.1. Situacao das Migrations e Banco de Dados

1. **As migrations do Flyway ja estao presentes no novo projeto.**
   - Todas as migracoes necessarias para criacao/alteracao do esquema de banco de dados ja foram aplicadas ao novo projeto.
2. **O esquema de banco de dados e considerado existente e consolidado.**
   - O planejamento aqui NAO deve alterar as migrations ja criadas.
   - O foco e USAR o banco existente, nao redesenha-lo.

### 9.2. Camadas de Software a Serem Criadas

3. **Todas as demais camadas de software ainda precisam ser criadas no novo projeto.** Isso inclui:
   - Modelos de dominio (equivalentes aos records Java: `MovimentoApi`, `Lancamento`, `MovimentoApiResponse`, `MovimentoApiResponseEvent`, enums).
   - Entidades de persistencia (equivalentes as entities JPA: `MovimentoApiEntity`, `LancamentoEntity`, `MaquinaEntity`, `BancoEntity`, etc.).
   - Repositorios de acesso a dados (equivalentes aos JPA repositories).
   - Logica de integracao com API PagBank (client HTTP, equivalente ao Feign Client).
   - Logica de processamento (extracao, paginacao, criacao de maquinas, insercao de lancamentos).
   - Servicos de criptografia para payload.
   - Endpoint REST para trigger.

4. **Foco nas entidades e no modelo de dados do lado da aplicacao:**
   - As tabelas envolvidas sao: `eventos.movimento_api`, `eventos.lancamento`, `eventos.maquina`, `eventos.banco`, `eventos.congregacao`, `eventos.departamento`.
   - Relacionamentos:
     - `Lancamento` N:1 `Maquina` (via `maquina_id`, FK com ON DELETE SET NULL)
     - `Lancamento` N:1 `Congregacao` (via `congregacao_id`)
     - `Lancamento` N:1 `Departamento` (via `departamento_id`)
     - `Maquina` N:1 `Banco` (via `banco_id`, FK com ON DELETE CASCADE)
     - `Maquina` N:1 `Congregacao` (via `congregacao_id`, FK com ON DELETE CASCADE)
     - `Maquina` N:1 `Departamento` (via `departamento_id`)

### 9.3. Limites de Arquitetura (o que NAO deve ser tratado)

5. **Nao detalhar arquitetura em termos de ports e adapters.**
   - Este documento NAO deve entrar em decisoes sobre ports, adapters, boundaries.
   - Essas decisoes sao tratadas em outro nivel de documentacao/projeto.

6. **Foco no planejamento das entidades para integracao com o banco existente.**

### 9.4. Regras Resumidas

7. **Regra 1 - Migrations fixas:** As migrations do Flyway JA estao no novo projeto e NAO devem ser alteradas neste planejamento.

8. **Regra 2 - Banco de dados pre-existente:** O banco de dados resultante dessas migrations e a base de verdade. O planejamento deve se adaptar a ele.

9. **Regra 3 - Criar as camadas de software restantes:** Devem ser planejadas e implementadas todas as camadas necessarias (dominio, repositories, services, clients, endpoints, etc.), exceto as migrations.

10. **Regra 4 - Sem detalhamento de ports/adapters:** Este documento NAO especifica detalhes sobre ports, adapters ou componentes de arquitetura de alto nivel.

11. **Regra 5 - Foco nas entidades de dominio/persistencia:** O principal resultado esperado e um planejamento claro das entidades (nome, campos, relacionamentos) que serao usadas para representar o dominio e conectar com o banco de dados existente.

### 9.5. Pontos de Atencao para Implementacao em Kotlin

- Os enums devem preservar os mapeamentos de codigo <-> nome para compatibilidade com o banco existente (que armazena o nome do enum, nao o codigo numerico).
- A logica de `ON CONFLICT DO NOTHING` deve ser mantida para deduplicacao de lancamentos.
- A criptografia AES-256/GCM deve ser compativel com os dados ja armazenados (mesmo algoritmo, mesmo formato IV+ciphertext+tag em Base64).
- O processamento concorrente com limites (semaforo) pode ser implementado usando coroutines Kotlin com `Semaphore` do kotlinx.coroutines.
- A constraint UNIQUE `(data_leitura, pagina)` em `movimento_api` deve ser respeitada.
- O pre-cadastro do banco PagBank (codigo "290") e pre-requisito para o funcionamento.
- O campo `@JsonNaming(SnakeCaseStrategy)` no record `Lancamento` indica que a API PagBank retorna campos em snake_case - o novo client HTTP deve mapear adequadamente.
- Os `@JsonCreator` dos enums aceitam tanto codigos numericos quanto nomes de enum - o novo sistema deve manter esse comportamento para flexibilidade.
- O `DateTimeUtil.toDate()` converte `String "YYYY-MM-DD"` para `java.util.Date` sem timezone explicito - considerar usar `LocalDate` no novo sistema para maior clareza.
