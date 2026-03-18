package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilterModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface CongregationRepository :
    JpaRepository<Congregation, UUID>,
    JpaSpecificationExecutor<Congregation> {
    fun findFiltered(
        filter: CongregationFilterModel?,
        pageable: Pageable,
    ): Page<Congregation> {
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Order.asc("nome"))
        val p = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)
        return findAll(CongregationSpecification.fromFilter(filter), p)
    }

    fun existsByCnpj(cnpj: String): Boolean

    fun existsByCnpjAndIdNot(
        cnpj: String,
        id: UUID,
    ): Boolean

    fun findAllByOrderByNomeAsc(): List<Congregation>

    fun findBySetorialIdIsNullAndAtivoTrueOrderByNomeAsc(): List<Congregation>
}
