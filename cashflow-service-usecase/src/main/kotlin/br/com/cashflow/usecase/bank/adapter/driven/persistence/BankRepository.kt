package br.com.cashflow.usecase.bank.adapter.driven.persistence

import br.com.cashflow.usecase.bank.entity.Bank
import java.util.UUID

interface BankRepository {
    fun findById(id: UUID): Bank?

    fun findAllByOrderByNomeAsc(): List<Bank>
}
