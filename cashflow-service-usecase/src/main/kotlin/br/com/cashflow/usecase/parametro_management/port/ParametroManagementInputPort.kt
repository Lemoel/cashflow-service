package br.com.cashflow.usecase.parametro_management.port

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilter
import br.com.cashflow.usecase.parametro.model.ParametroPage
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroCreateRequest
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroUpdateRequest
import java.util.UUID

interface ParametroManagementInputPort {
    fun create(request: ParametroCreateRequest): Parametro

    fun update(
        id: UUID,
        request: ParametroUpdateRequest,
    ): Parametro

    fun findById(id: UUID): Parametro?

    fun findAll(
        filter: ParametroFilter?,
        page: Int,
        size: Int,
    ): ParametroPage

    fun findChavesForDropdown(): List<Pair<String, String>>

    fun delete(id: UUID)
}
