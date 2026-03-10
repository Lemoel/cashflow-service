package br.com.cashflow.usecase.parametro.port

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilter
import br.com.cashflow.usecase.parametro.model.ParametroPage
import java.util.UUID

interface ParametroOutputPort {
    fun save(parametro: Parametro): Parametro

    fun findById(id: UUID): Parametro?

    fun findWithFilters(
        filter: ParametroFilter?,
        page: Int,
        size: Int,
    ): ParametroPage

    fun findAllOrderByChave(): List<Parametro>

    fun existsByChave(chave: String): Boolean

    fun existsByChaveExcludingId(
        chave: String,
        excludeId: UUID?,
    ): Boolean

    fun deleteById(id: UUID)
}
