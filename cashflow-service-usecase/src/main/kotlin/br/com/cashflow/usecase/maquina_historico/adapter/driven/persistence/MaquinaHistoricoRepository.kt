package br.com.cashflow.usecase.maquina_historico.adapter.driven.persistence

import br.com.cashflow.usecase.maquina_historico.entity.MaquinaHistorico
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MaquinaHistoricoRepository :
    CrudRepository<MaquinaHistorico, UUID>,
    MaquinaHistoricoRepositoryCustom {
    @Modifying
    @Query(
        "UPDATE maquina_historico SET data_fim = CURRENT_TIMESTAMP WHERE maquina_id = :maquinaId AND data_fim IS NULL",
    )
    fun fecharPeriodoAtual(
        @Param("maquinaId") maquinaId: UUID,
    )
}

interface MaquinaHistoricoRepositoryCustom {
    fun findByMaquinaIdOrderByDataInicioDesc(maquinaId: UUID): List<MaquinaHistoricoItemRow>
}

data class MaquinaHistoricoItemRow(
    val id: UUID,
    val maquinaId: UUID,
    val congregacaoId: UUID?,
    val congregacaoNome: String?,
    val departamentoId: UUID?,
    val departamentoNome: String?,
    val dataInicio: java.time.Instant,
    val dataFim: java.time.Instant?,
)
