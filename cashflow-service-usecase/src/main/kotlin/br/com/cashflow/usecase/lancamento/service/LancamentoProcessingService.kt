package br.com.cashflow.usecase.lancamento.service

import br.com.cashflow.usecase.bank.entity.Bank
import br.com.cashflow.usecase.lancamento.entity.Lancamento
import br.com.cashflow.usecase.lancamento.port.LancamentoOutputPort
import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.port.MaquinaOutputPort
import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import br.com.cashflow.usecase.movimento_api.entity.StatusProcessamentoEnum
import br.com.cashflow.usecase.movimento_api.port.MovimentoApiOutputPort
import br.com.cashflow.usecase.pagbank.client.LancamentoDetalhe
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class LancamentoProcessingService(
    private val lancamentoOutputPort: LancamentoOutputPort,
    private val maquinaOutputPort: MaquinaOutputPort,
    private val movimentoApiOutputPort: MovimentoApiOutputPort,
) {
    @Transactional
    fun processarLancamentos(
        dataLeitura: LocalDate,
        detalhes: List<LancamentoDetalhe>,
        pagBank: Bank,
    ) {
        val movimentoApi =
            movimentoApiOutputPort.findByDataLeituraAndPagina(dataLeitura, 1)
                ?: return

        if (movimentoApi.status != StatusProcessamentoEnum.RECEBIDO) {
            log.debug("MovimentoApi já processado ou em erro para data={} pagina=1", dataLeitura)
            return
        }

        if (detalhes.isEmpty()) {
            atualizarStatusMovimento(movimentoApi, StatusProcessamentoEnum.PROCESSADA)
            return
        }

        try {
            val seriesLeitor =
                detalhes
                    .mapNotNull {
                        it.numeroSerieLeitor?.takeIf { serie ->
                            serie.isNotBlank()
                        }
                    }.toSet()
            val maquinasPorSerie = buscarOuCriarMaquinas(seriesLeitor, pagBank)

            for (detalhe in detalhes) {
                val maquina =
                    detalhe.numeroSerieLeitor
                        ?.takeIf {
                            it.isNotBlank()
                        }?.let { maquinasPorSerie[it] }
                lancamentoOutputPort.insertIgnorandoDuplicata(detalhe.toModel(maquina))
            }

            atualizarStatusMovimento(movimentoApi, StatusProcessamentoEnum.PROCESSADA)
        } catch (error: Exception) {
            log.error("Erro ao processar lançamentos para data={}", dataLeitura, error)
            atualizarStatusMovimento(movimentoApi, StatusProcessamentoEnum.ERRO_PROCESSAMENTO)
            throw error
        }
    }

    private fun atualizarStatusMovimento(
        movimentoApi: MovimentoApi,
        status: StatusProcessamentoEnum,
    ) {
        movimentoApi.status = status
        movimentoApiOutputPort.save(movimentoApi)
    }

    private fun buscarOuCriarMaquinas(
        numeroSeries: Set<String>,
        pagBank: Bank,
    ): Map<String, Maquina> {
        if (numeroSeries.isEmpty()) return emptyMap()

        val existentes = maquinaOutputPort.findByNumeroSerieLeitorIn(numeroSeries)
        val mapa =
            existentes
                .mapNotNull { maquina ->
                    maquina.numeroSerieLeitor?.let { it to maquina }
                }.toMap()
                .toMutableMap()
        val faltantes = numeroSeries - mapa.keys

        for (serie in faltantes) {
            val novaMaquina =
                Maquina(
                    numeroSerieLeitor = serie,
                    bancoId = pagBank.id,
                    ativo = true,
                    creationUserId = CREATION_USER_BOT,
                )
            val salva = maquinaOutputPort.save(novaMaquina)
            salva.numeroSerieLeitor?.let { mapa[it] = salva }
        }

        return mapa
    }

    companion object {
        private val log = LoggerFactory.getLogger(LancamentoProcessingService::class.java)
        private const val CREATION_USER_BOT = "BOT"
    }
}

fun LancamentoDetalhe.toModel(maquina: Maquina?): Lancamento =
    Lancamento(
        nsu = nsu,
        tid = tid,
        codigoTransacao = codigoTransacao,
        parcela = parcela,
        tipoEvento = tipoEvento,
        meioCaptura = meioCaptura,
        valorParcela = valorParcela,
        meioPagamento = meioPagamento,
        estabelecimento = estabelecimento,
        pagamentoPrazo = pagamentoPrazo,
        taxaIntermediacao = taxaIntermediacao,
        numeroSerieLeitor = numeroSerieLeitor,
        valorTotalTransacao = valorTotalTransacao,
        dataInicialTransacao = dataInicialTransacao,
        horaInicialTransacao = horaInicialTransacao,
        dataPrevistaPagamento = dataPrevistaPagamento,
        valorLiquidoTransacao = valorLiquidoTransacao,
        valorOriginalTransacao = valorOriginalTransacao,
        maquinaId = maquina?.id,
        congregacaoId = maquina?.congregacaoId,
        departamentoId = maquina?.departamentoId,
        creationUserId = "BOT",
    )
