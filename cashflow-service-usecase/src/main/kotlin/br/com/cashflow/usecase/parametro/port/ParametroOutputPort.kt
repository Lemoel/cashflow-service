package br.com.cashflow.usecase.parametro.port

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import br.com.cashflow.usecase.parametro.model.ParametroPageModel
import java.util.UUID

interface ParametroOutputPort {
    fun save(parametro: Parametro): Parametro

    fun findById(id: UUID): Parametro?

    fun findWithFilters(
        filter: ParametroFilterModel?,
        page: Int,
        size: Int,
    ): ParametroPageModel

    fun findAllChaveOrderByChave(): List<String>

    fun existsByChave(chave: String): Boolean

    fun existsByChaveExcludingId(
        chave: String,
        excludeId: UUID?,
    ): Boolean

    fun existsById(id: UUID): Boolean

    fun deleteById(id: UUID)
}
