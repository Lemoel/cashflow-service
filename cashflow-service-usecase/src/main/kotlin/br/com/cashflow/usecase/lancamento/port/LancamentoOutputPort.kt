package br.com.cashflow.usecase.lancamento.port

import br.com.cashflow.usecase.lancamento.entity.Lancamento

interface LancamentoOutputPort {
    fun insertIgnorandoDuplicata(lancamento: Lancamento): Unit

    fun batchInsertIgnorandoDuplicatas(lancamentos: List<Lancamento>)
}
