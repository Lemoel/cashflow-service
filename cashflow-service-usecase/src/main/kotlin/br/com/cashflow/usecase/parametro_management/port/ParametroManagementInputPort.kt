package br.com.cashflow.usecase.parametro_management.port

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import br.com.cashflow.usecase.parametro.model.ParametroPageModel
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroCreateRequestDto
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroUpdateRequestDto
import java.util.UUID

interface ParametroManagementInputPort {
    fun create(request: ParametroCreateRequestDto): Parametro

    fun update(
        id: UUID,
        request: ParametroUpdateRequestDto,
    ): Parametro

    fun findById(id: UUID): Parametro?

    fun findAll(
        filter: ParametroFilterModel?,
        page: Int,
        size: Int,
    ): ParametroPageModel

    fun findChavesForDropdown(): List<Pair<String, String>>

    fun delete(id: UUID)
}
