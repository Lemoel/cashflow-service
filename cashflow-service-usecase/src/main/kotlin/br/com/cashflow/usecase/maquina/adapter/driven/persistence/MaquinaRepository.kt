package br.com.cashflow.usecase.maquina.adapter.driven.persistence

import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface MaquinaRepository :
    CrudRepository<Maquina, UUID>,
    MaquinaRepositoryCustom {
    fun existsByNumeroSerieLeitor(numeroSerieLeitor: String): Boolean
}

interface MaquinaRepositoryCustom {
    fun findByIdWithDetalhes(id: UUID): MaquinaComCongregacao?

    fun findWithFiltersComDetalhes(
        maquinaId: String?,
        congregacao: String?,
        banco: String?,
        departamentoId: UUID?,
        page: Int,
        size: Int,
    ): MaquinaQueryResult

    fun findParaSelecaoHistorico(
        tenantId: UUID?,
        congregacaoId: UUID?,
        numeroSerieLeitor: String?,
        page: Int,
        size: Int,
    ): MaquinaQueryResult
}

data class MaquinaQueryResult(
    val items: List<MaquinaComCongregacao>,
    val total: Long,
)
