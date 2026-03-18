package br.com.cashflow.usecase.acesso.model

import java.time.Instant
import java.util.UUID

data class AcessoListItem(
    val email: String,
    val nome: String?,
    val telefone: String?,
    val tipoAcesso: String,
    val ativo: Boolean,
    val createdDate: Instant?,
    val lastModifiedDate: Instant?,
    val congregacaoId: UUID?,
    val congregacaoNome: String?,
)
