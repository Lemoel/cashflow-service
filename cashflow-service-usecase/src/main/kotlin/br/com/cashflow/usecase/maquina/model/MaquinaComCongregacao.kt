package br.com.cashflow.usecase.maquina.model

import java.time.Instant
import java.util.UUID

data class MaquinaComCongregacao(
    val id: UUID,
    val maquinaId: String,
    val congregacaoId: UUID?,
    val congregacaoNome: String,
    val bancoId: UUID?,
    val bancoNome: String,
    val departamentoId: UUID?,
    val departamentoNome: String?,
    val ativo: Boolean,
    val version: Long?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)
