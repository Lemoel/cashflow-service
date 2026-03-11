package br.com.cashflow.usecase.movimento_extraction.port

import java.time.LocalDate

interface MovimentoExtractionInputPort {
    fun extrairTodosDiasPendentes()

    fun extrairDia(data: LocalDate)
}
