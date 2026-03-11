package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface ParametroRepository :
    CrudRepository<Parametro, UUID>,
    ParametroRepositoryCustom {
    fun existsByChave(chave: String): Boolean

    fun existsByChaveAndIdNot(
        chave: String,
        id: UUID,
    ): Boolean

    fun findAllByOrderByChaveAsc(): List<Parametro>
}

interface ParametroRepositoryCustom {
    fun findWithFilters(
        filter: ParametroFilterModel?,
        pageable: Pageable,
    ): Page<Parametro>
}
