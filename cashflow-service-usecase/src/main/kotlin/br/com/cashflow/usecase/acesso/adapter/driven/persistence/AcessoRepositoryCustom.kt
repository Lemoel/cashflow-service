package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import br.com.cashflow.usecase.acesso.model.AcessoFilter
import br.com.cashflow.usecase.acesso.model.AcessoListItem
import org.springframework.data.domain.Pageable

interface AcessoRepositoryCustom {
    fun findFiltered(
        filter: AcessoFilter?,
        pageable: Pageable,
    ): List<AcessoListItem>

    fun countFiltered(filter: AcessoFilter?): Long
}
