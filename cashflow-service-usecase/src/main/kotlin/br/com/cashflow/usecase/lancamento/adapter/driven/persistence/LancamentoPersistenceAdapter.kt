package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

import br.com.cashflow.usecase.lancamento.entity.Lancamento
import br.com.cashflow.usecase.lancamento.port.LancamentoOutputPort
import org.springframework.stereotype.Component

@Component
class LancamentoPersistenceAdapter(
    private val lancamentoRepository: LancamentoRepository,
) : LancamentoOutputPort {
    override fun insertIgnorandoDuplicata(lancamento: Lancamento) {
        lancamentoRepository.insertIgnorandoDuplicata(lancamento)
    }
}
