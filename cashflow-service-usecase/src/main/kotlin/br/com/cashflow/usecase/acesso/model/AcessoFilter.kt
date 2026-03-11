package br.com.cashflow.usecase.acesso.model

import java.util.UUID

data class AcessoFilter(
    val email: String? = null,
    val congregacaoId: UUID? = null,
    val perfil: String? = null,
    val ativo: Boolean? = null,
)
