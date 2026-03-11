package br.com.cashflow.usecase.user_authentication.model

import java.util.UUID

data class UsuarioResponseModel(
    val id: String,
    val nome: String?,
    val email: String,
    val perfil: String,
    val tenantId: UUID?,
    val tenantNome: String?,
)

data class LoginResponseModel(
    val token: String,
    val refreshToken: String?,
    val expiresIn: Long,
    val usuario: UsuarioResponseModel,
)
