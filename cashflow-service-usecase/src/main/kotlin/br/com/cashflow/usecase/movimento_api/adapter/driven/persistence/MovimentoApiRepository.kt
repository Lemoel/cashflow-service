package br.com.cashflow.usecase.movimento_api.adapter.driven.persistence

import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import java.util.UUID

interface MovimentoApiRepository : CrudRepository<MovimentoApi, UUID> {
    fun findFirstByOrderByDataLeituraDesc(): MovimentoApi?

    fun findByDataLeituraAndPagina(
        dataLeitura: LocalDate,
        pagina: Int,
    ): MovimentoApi?
}
