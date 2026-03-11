package br.com.cashflow.usecase.bank.port

import br.com.cashflow.usecase.bank.entity.Bank
import java.util.UUID

interface BankOutputPort {
    fun findById(id: UUID): Bank?

    fun findByCodigo(codigo: String): Bank?

    fun findAllOrderByNomeAsc(): List<Bank>
}
