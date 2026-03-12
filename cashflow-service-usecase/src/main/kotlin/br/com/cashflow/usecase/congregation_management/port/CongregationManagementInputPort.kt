package br.com.cashflow.usecase.congregation_management.port

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilterModel
import br.com.cashflow.usecase.congregation.model.CongregationPageModel
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationCreateRequestDto
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationUpdateRequestDto
import java.util.UUID

interface CongregationManagementInputPort {
    fun create(request: CongregationCreateRequestDto): Congregation

    fun update(
        id: UUID,
        request: CongregationUpdateRequestDto,
    ): Congregation

    fun findById(id: UUID): Congregation?

    fun findAll(
        filter: CongregationFilterModel?,
        page: Int,
        size: Int,
    ): CongregationPageModel

    fun findListForDropdown(): List<Pair<UUID, String>>

    fun findSetoriais(): List<Pair<UUID, String>>

    fun delete(id: UUID)
}
