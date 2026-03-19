package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

import br.com.cashflow.usecase.lancamento.entity.Lancamento
import br.com.cashflow.usecase.lancamento.port.LancamentoOutputPort
import org.springframework.stereotype.Component

@Component
class LancamentoPersistenceAdapter(
    private val lancamentoRepository: LancamentoRepository,
) : LancamentoOutputPort {
    override fun insertIgnorandoDuplicata(lancamento: Lancamento) {
        lancamentoRepository.insertIgnorandoDuplicata(
            nsu = lancamento.nsu,
            tid = lancamento.tid,
            codigoTransacao = lancamento.codigoTransacao,
            parcela = lancamento.parcela,
            tipoEvento = lancamento.tipoEvento.code,
            meioCaptura = lancamento.meioCaptura.code,
            valorParcela = lancamento.valorParcela,
            meioPagamento = lancamento.meioPagamento.code,
            estabelecimento = lancamento.estabelecimento,
            pagamentoPrazo = lancamento.pagamentoPrazo,
            taxaIntermediacao = lancamento.taxaIntermediacao,
            numeroSerieLeitor = lancamento.numeroSerieLeitor,
            valorTotalTransacao = lancamento.valorTotalTransacao,
            dataInicialTransacao = lancamento.dataInicialTransacao,
            horaInicialTransacao = lancamento.horaInicialTransacao,
            dataPrevistaPagamento = lancamento.dataPrevistaPagamento,
            valorLiquidoTransacao = lancamento.valorLiquidoTransacao,
            valorOriginalTransacao = lancamento.valorOriginalTransacao,
            maquinaId = lancamento.maquinaId,
            congregacaoId = lancamento.congregacaoId,
            departamentoId = lancamento.departamentoId,
            createdBy = lancamento.createdBy ?: "system",
            lastModifiedBy = lancamento.lastModifiedBy ?: lancamento.createdBy ?: "system",
        )
    }

    override fun batchInsertIgnorandoDuplicatas(lancamentos: List<Lancamento>) {
        if (lancamentos.isEmpty()) return
        val batchSize = 500
        lancamentos.chunked(batchSize).forEach { chunk ->
            lancamentoRepository.batchInsertIgnorandoDuplicatas(chunk)
        }
    }
}
