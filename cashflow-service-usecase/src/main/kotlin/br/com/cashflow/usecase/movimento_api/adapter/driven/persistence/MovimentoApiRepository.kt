package br.com.cashflow.usecase.movimento_api.adapter.driven.persistence

import br.com.cashflow.usecase.movimento_api.entity.MovimentoApi
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface MovimentoApiRepository : JpaRepository<MovimentoApi, UUID> {
    fun findFirstByOrderByDataLeituraDesc(): MovimentoApi?

    fun findByDataLeituraAndPagina(
        dataLeitura: LocalDate,
        pagina: Int,
    ): MovimentoApi?
}
