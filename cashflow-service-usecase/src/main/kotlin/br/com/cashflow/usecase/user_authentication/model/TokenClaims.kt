package br.com.cashflow.usecase.user_authentication.model

import java.util.UUID

data class TokenClaims(
    val sub: String,
    val email: String,
    val perfil: String,
    val tenantId: UUID?,
)
