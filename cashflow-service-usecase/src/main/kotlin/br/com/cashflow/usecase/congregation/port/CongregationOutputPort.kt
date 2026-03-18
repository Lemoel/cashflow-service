package br.com.cashflow.usecase.congregation.port

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilterModel
import br.com.cashflow.usecase.congregation.model.CongregationPageModel
import java.util.UUID

interface CongregationOutputPort {
    fun save(congregation: Congregation): Congregation

    fun findById(id: UUID): Congregation?

    fun findAll(
        filter: CongregationFilterModel?,
        page: Int,
        size: Int,
    ): CongregationPageModel

    fun findAllOrderByNome(): List<Pair<UUID, String>>

    fun findSetoriais(): List<Pair<UUID, String>>

    fun existsByCnpjExcludingId(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean

    fun deleteById(id: UUID)
}
