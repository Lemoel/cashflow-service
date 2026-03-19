package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ParametroRepository :
    JpaRepository<Parametro, UUID>,
    JpaSpecificationExecutor<Parametro> {
    fun findWithFilters(
        filter: ParametroFilterModel?,
        pageable: Pageable,
    ): Page<Parametro> {
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Order.asc("chave"))
        val p = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)
        return findAll(ParametroSpecification.fromFilter(filter), p)
    }

    fun existsByChave(chave: String): Boolean

    fun existsByChaveAndIdNot(
        chave: String,
        id: UUID,
    ): Boolean

    fun findAllByOrderByChaveAsc(): List<Parametro>

    @Query("SELECT p.chave FROM Parametro p ORDER BY p.chave")
    fun findAllChaveOrderByChaveAsc(): List<String>
}
