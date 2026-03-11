package br.com.cashflow.usecase.maquina_historico.model

import java.time.Instant
import java.util.UUID

data class MaquinaHistoricoItemModel(
    val id: UUID,
    val maquinaId: UUID,
    val congregacaoId: UUID?,
    val congregacaoNome: String?,
    val departamentoId: UUID?,
    val departamentoNome: String?,
    val dataInicio: Instant,
    val dataFim: Instant?,
)
