package br.com.cashflow.usecase.pagbank.port

import java.time.LocalDate

interface PagBankOutputPort {
    fun buscarMovimentos(
        data: LocalDate,
        pagina: Int,
    ): RespostaMovimento
}
