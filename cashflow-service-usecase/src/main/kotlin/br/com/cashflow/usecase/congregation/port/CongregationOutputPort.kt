package br.com.cashflow.usecase.congregation.port

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilter
import br.com.cashflow.usecase.congregation.model.CongregationPage
import java.util.UUID

interface CongregationOutputPort {
    fun save(congregation: Congregation): Congregation

    fun findById(id: UUID): Congregation?

    fun findAll(
        filter: CongregationFilter?,
        page: Int,
        size: Int,
    ): CongregationPage

    fun findAllOrderByNome(): List<Congregation>

    fun findSetoriais(): List<Congregation>

    fun existsByCnpjExcludingId(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean

    fun deleteById(id: UUID)
}
