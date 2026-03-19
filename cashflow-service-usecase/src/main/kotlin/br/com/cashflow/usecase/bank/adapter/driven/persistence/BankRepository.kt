package br.com.cashflow.usecase.bank.adapter.driven.persistence

import br.com.cashflow.usecase.bank.entity.Bank
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BankRepository : JpaRepository<Bank, UUID> {
    fun findByCodigo(codigo: String): Bank?

    fun findAllByOrderByNomeAsc(): List<Bank>
}
