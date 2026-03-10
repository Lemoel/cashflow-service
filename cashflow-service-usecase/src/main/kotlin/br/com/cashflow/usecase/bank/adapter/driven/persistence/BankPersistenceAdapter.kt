package br.com.cashflow.usecase.bank.adapter.driven.persistence

import br.com.cashflow.usecase.bank.entity.Bank
import br.com.cashflow.usecase.bank.port.BankOutputPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BankPersistenceAdapter(
    private val bankRepository: BankRepository,
) : BankOutputPort {
    override fun findById(id: UUID): Bank? = bankRepository.findById(id)

    override fun findAllOrderByNomeAsc(): List<Bank> = bankRepository.findAllByOrderByNomeAsc()
}
