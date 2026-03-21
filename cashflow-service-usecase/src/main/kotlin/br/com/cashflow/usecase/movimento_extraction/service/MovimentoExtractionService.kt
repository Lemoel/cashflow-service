package br.com.cashflow.usecase.movimento_extraction.service

import br.com.cashflow.commons.tenant.tenantCoroutineContext
import br.com.cashflow.usecase.bank.entity.Bank
import br.com.cashflow.usecase.bank.port.BankOutputPort
import br.com.cashflow.usecase.lancamento.service.LancamentoProcessingService
import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import br.com.cashflow.usecase.movimento_api.entity.StatusProcessamentoEnum
import br.com.cashflow.usecase.movimento_api.port.MovimentoApiOutputPort
import br.com.cashflow.usecase.movimento_extraction.port.MovimentoExtractionInputPort
import br.com.cashflow.usecase.pagbank.config.PagBankApiProperties
import br.com.cashflow.usecase.pagbank.port.PagBankOutputPort
import br.com.cashflow.usecase.pagbank.port.RespostaMovimento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class MovimentoExtractionService(
    private val movimentoApiOutputPort: MovimentoApiOutputPort,
    private val bankOutputPort: BankOutputPort,
    private val pagBankOutputPort: PagBankOutputPort,
    private val lancamentoProcessingService: LancamentoProcessingService,
    private val pagBankApiProperties: PagBankApiProperties,
) : MovimentoExtractionInputPort {
    override fun extrairTodosDiasPendentes() {
        val pagBank =
            bankOutputPort.findByCodigo(CODIGO_PAGBANK)
                ?: throw IllegalStateException("PagBank (código $CODIGO_PAGBANK) não encontrado")

        val ultimoMovimentoProcessado = movimentoApiOutputPort.findFirstByOrderByDataLeituraDesc()
        val dataInicio = parseDataInicio()
        val ultimaDataProcessada = ultimoMovimentoProcessado?.dataLeitura ?: dataInicio.minusDays(1)
        val dataReferencia = LocalDate.now(ZoneOffset.UTC).minusDays(1)
        val primeiraDataPendente = ultimaDataProcessada.plusDays(1)

        if (primeiraDataPendente.isAfter(dataReferencia)) return

        val datasPendentes = primeiraDataPendente.datesUntil(dataReferencia.plusDays(1)).toList()

        if (datasPendentes.isEmpty()) return

        val paginaInicialPrimeiroDia =
            (
                ultimoMovimentoProcessado?.pagina?.takeIf {
                    it >= 1
                } ?: 1
            ).coerceAtLeast(1)

        val semaphore = Semaphore(3)

        runBlocking(Dispatchers.IO + tenantCoroutineContext()) {
            datasPendentes
                .mapIndexed { index, data ->
                    async {
                        semaphore.withPermit {
                            try {
                                val paginaInicial = if (index == 0) paginaInicialPrimeiroDia else 1
                                processarDia(data, paginaInicial, pagBank)
                            } catch (error: Exception) {
                                log.error("Erro ao processar dia {}", data, error)
                            }
                        }
                    }
                }.forEach { it.await() }
        }
    }

    override fun extrairDia(data: LocalDate) {
        val pagBank =
            bankOutputPort.findByCodigo(CODIGO_PAGBANK)
                ?: throw IllegalStateException("PagBank (código $CODIGO_PAGBANK) não encontrado")
        processarDia(data, 1, pagBank)
    }

    private fun parseDataInicio(): LocalDate = LocalDate.parse(pagBankApiProperties.inicio, DateTimeFormatter.ISO_LOCAL_DATE)

    private fun processarDia(
        data: LocalDate,
        paginaInicial: Int,
        pagBank: Bank,
    ) {
        val resposta = pagBankOutputPort.buscarMovimentos(data, paginaInicial)

        when (resposta) {
            is RespostaMovimento.NaoValidada -> {
                log.debug("Resposta ignorada para {} (header validado != true)", data)
                return
            }
            is RespostaMovimento.SemConteudo -> return
            is RespostaMovimento.ErroDesserializacao -> {
                salvarMovimentoApi(
                    null,
                    data,
                    paginaInicial,
                    0,
                    1,
                    StatusProcessamentoEnum.ERRO_PAYLOAD,
                )
                return
            }
            is RespostaMovimento.Sucesso -> {
                processarRespostaSucesso(data, paginaInicial, resposta, pagBank)
            }
        }
    }

    private fun processarRespostaSucesso(
        data: LocalDate,
        paginaInicial: Int,
        resposta: RespostaMovimento.Sucesso,
        pagBank: Bank,
    ) {
        salvarMovimentoApi(
            resposta.payloadCriptografado,
            data,
            paginaInicial,
            resposta.totalElementos,
            resposta.totalPaginas,
            StatusProcessamentoEnum.RECEBIDO,
        )

        val todosDetalhes = resposta.detalhes.toMutableList()

        for (pagina in (paginaInicial + 1)..resposta.totalPaginas) {
            val respostaPagina = pagBankOutputPort.buscarMovimentos(data, pagina)
            when (respostaPagina) {
                is RespostaMovimento.NaoValidada -> {
                    salvarMovimentoApi(
                        null,
                        data,
                        pagina,
                        0,
                        resposta.totalPaginas,
                        StatusProcessamentoEnum.ERRO_COMUNICACAO,
                    )
                    throw RuntimeException("Header validado não true na página $pagina")
                }
                is RespostaMovimento.SemConteudo -> break
                is RespostaMovimento.ErroDesserializacao -> {
                    salvarMovimentoApi(
                        null,
                        data,
                        pagina,
                        0,
                        resposta.totalPaginas,
                        StatusProcessamentoEnum.ERRO_PAYLOAD,
                    )
                    break
                }
                is RespostaMovimento.Sucesso -> {
                    salvarMovimentoApi(
                        respostaPagina.payloadCriptografado,
                        data,
                        pagina,
                        respostaPagina.totalElementos,
                        resposta.totalPaginas,
                        StatusProcessamentoEnum.RECEBIDO,
                    )
                    todosDetalhes.addAll(respostaPagina.detalhes)
                }
            }
        }

        lancamentoProcessingService.processarLancamentos(data, todosDetalhes, pagBank)
    }

    private fun salvarMovimentoApi(
        payload: String?,
        dataLeitura: LocalDate,
        pagina: Int,
        totalElementos: Int,
        totalPaginas: Int,
        status: StatusProcessamentoEnum,
    ) {
        val movimento =
            MovimentoApi(
                payload = payload,
                status = status,
                pagina = pagina,
                totalElementos = totalElementos,
                totalPaginas = totalPaginas,
                dataLeitura = dataLeitura,
            )
        movimentoApiOutputPort.save(movimento)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MovimentoExtractionService::class.java)
        private const val CODIGO_PAGBANK = "290"
    }
}
