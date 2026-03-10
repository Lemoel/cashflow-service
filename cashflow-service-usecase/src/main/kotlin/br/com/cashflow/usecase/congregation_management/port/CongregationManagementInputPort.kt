package br.com.cashflow.usecase.congregation_management.port

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilter
import br.com.cashflow.usecase.congregation.model.CongregationPage
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationCreateRequest
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationUpdateRequest
import java.util.UUID

interface CongregationManagementInputPort {
    fun create(request: CongregationCreateRequest): Congregation

    fun update(
        id: UUID,
        request: CongregationUpdateRequest,
    ): Congregation

    fun findById(id: UUID): Congregation?

    fun findAll(
        filter: CongregationFilter?,
        page: Int,
        size: Int,
    ): CongregationPage

    fun findListForDropdown(): List<Pair<UUID, String>>

    fun findSetoriais(): List<Pair<UUID, String>>

    fun delete(id: UUID)

    fun isCnpjAvailable(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean
}
