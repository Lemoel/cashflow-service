package br.com.cashflow.usecase.maquina.port

import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import br.com.cashflow.usecase.maquina.model.MaquinaPage
import java.util.UUID

interface MaquinaOutputPort {
    fun save(maquina: Maquina): Maquina

    fun saveAll(maquinas: List<Maquina>): List<Maquina>

    fun findById(id: UUID): Maquina?

    fun findByNumeroSerieLeitorIn(numeroSerieLeitor: Collection<String>): List<Maquina>

    fun findByIdWithDetalhes(id: UUID): MaquinaComCongregacao?

    fun existsByNumeroSerieLeitor(numeroSerieLeitor: String): Boolean

    fun deleteById(id: UUID)

    fun findWithFiltersComDetalhes(
        maquinaId: String?,
        congregacao: String?,
        banco: String?,
        departamentoId: UUID?,
        page: Int,
        size: Int,
    ): MaquinaPage

    fun findParaSelecaoHistorico(
        tenantId: UUID?,
        congregacaoId: UUID?,
        numeroSerieLeitor: String?,
        page: Int,
        size: Int,
    ): MaquinaPage
}
