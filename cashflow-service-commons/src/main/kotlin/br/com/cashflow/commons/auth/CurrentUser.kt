package br.com.cashflow.commons.auth

import java.util.UUID

data class CurrentUser(
    val email: String,
    val perfil: String,
    val tenantId: UUID?,
)
