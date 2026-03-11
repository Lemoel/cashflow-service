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

    fun findAllOrderByNome(): List<Congregation>

    fun findSetoriais(): List<Congregation>

    fun existsByCnpjExcludingId(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean

    fun deleteById(id: UUID)
}
