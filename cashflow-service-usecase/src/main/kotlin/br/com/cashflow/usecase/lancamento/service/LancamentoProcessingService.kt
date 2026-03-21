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
                    .mapNotNull { canonSerie(it.numeroSerieLeitor) }
                    .toSet()
            val maquinasPorSerie = buscarOuCriarMaquinas(seriesLeitor, pagBank)
            val lancamentos =
                detalhes.map { detalhe ->
                    val maquina =
                        canonSerie(detalhe.numeroSerieLeitor)?.let { maquinasPorSerie[it] }
                    detalhe.toModel(maquina)
                }
            lancamentoOutputPort.batchInsertIgnorandoDuplicatas(lancamentos)

            atualizarStatusMovimento(movimentoApi, StatusProcessamentoEnum.PROCESSADA)
        } catch (error: Exception) {
            log.error("Erro ao processar lançamentos para data={}", dataLeitura, error)
            atualizarStatusMovimento(movimentoApi, StatusProcessamentoEnum.ERRO_PROCESSAMENTO)
            throw error
        }
    }

    private fun canonSerie(numeroSerieLeitor: String?): String? = numeroSerieLeitor?.trim()?.takeIf { it.isNotBlank() }

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
                    canonSerie(maquina.numeroSerieLeitor)?.let { it to maquina }
                }.toMap()
                .toMutableMap()
        val faltantes = numeroSeries - mapa.keys
        if (faltantes.isEmpty()) return mapa

        val novasMaquinas =
            faltantes.map { serie ->
                Maquina(
                    numeroSerieLeitor = serie,
                    bancoId = pagBank.id!!,
                    ativo = true,
                )
            }
        val salvas = maquinaOutputPort.saveAll(novasMaquinas)
        salvas.forEach { maquina ->
            canonSerie(maquina.numeroSerieLeitor)?.let { mapa[it] = maquina }
        }
        return mapa
    }

    companion object {
        private val log = LoggerFactory.getLogger(LancamentoProcessingService::class.java)
    }
}

fun LancamentoDetalhe.toModel(maquina: Maquina?): Lancamento {
    val lancamento =
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
        )
    lancamento.createdBy = "BOT"
    lancamento.lastModifiedBy = "BOT"
    return lancamento
}
