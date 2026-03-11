package br.com.cashflow.usecase.movimento_api.port

import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import java.time.LocalDate

interface MovimentoApiOutputPort {
    fun save(movimento: MovimentoApi): MovimentoApi

    fun findFirstByOrderByDataLeituraDesc(): MovimentoApi?

    fun findByDataLeituraAndPagina(
        dataLeitura: LocalDate,
        pagina: Int,
    ): MovimentoApi?
}
