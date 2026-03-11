package br.com.cashflow.usecase.maquina_management.port

import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import br.com.cashflow.usecase.maquina.model.MaquinaPage
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItemModel
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaCreateRequestDto
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaUpdateRequestDto
import java.util.UUID

interface MaquinaManagementInputPort {
    fun create(request: MaquinaCreateRequestDto): MaquinaComCongregacao

    fun update(
        id: UUID,
        request: MaquinaUpdateRequestDto,
    ): MaquinaComCongregacao

    fun findById(id: UUID): MaquinaComCongregacao?

    fun search(
        maquinaId: String?,
        congregacao: String?,
        banco: String?,
        departamentoId: UUID?,
        page: Int,
        size: Int,
    ): MaquinaPage

    fun listForOptions(
        tenantId: UUID?,
        congregacaoId: UUID?,
        numeroSerie: String?,
        page: Int,
        size: Int,
    ): MaquinaPage

    fun listHistoricoByMaquinaId(maquinaId: UUID): List<MaquinaHistoricoItemModel>

    fun delete(id: UUID)
}
