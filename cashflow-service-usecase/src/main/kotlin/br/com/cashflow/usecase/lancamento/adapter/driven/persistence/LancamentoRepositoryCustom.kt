package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

import br.com.cashflow.usecase.lancamento.entity.Lancamento

interface LancamentoRepositoryCustom {
    fun batchInsertIgnorandoDuplicatas(lancamentos: List<Lancamento>)
}
