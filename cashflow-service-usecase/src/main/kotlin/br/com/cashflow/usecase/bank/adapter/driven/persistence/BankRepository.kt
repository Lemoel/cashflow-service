package br.com.cashflow.usecase.bank.adapter.driven.persistence

import br.com.cashflow.usecase.bank.entity.Bank
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface BankRepository : CrudRepository<Bank, UUID> {
    fun findByCodigo(codigo: String): Bank?

    fun findAllByOrderByNomeAsc(): List<Bank>
}
