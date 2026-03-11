package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilterModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface CongregationRepository :
    CrudRepository<Congregation, UUID>,
    CongregationRepositoryCustom {
    fun existsByCnpj(cnpj: String): Boolean

    fun existsByCnpjAndIdNot(
        cnpj: String,
        id: UUID,
    ): Boolean

    fun findAllByOrderByNomeAsc(): List<Congregation>

    fun findBySetorialIdIsNullAndAtivoTrueOrderByNomeAsc(): List<Congregation>
}

interface CongregationRepositoryCustom {
    fun findFiltered(
        filter: CongregationFilterModel?,
        pageable: Pageable,
    ): Page<Congregation>
}
