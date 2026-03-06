package br.com.cashflow.usecase.user_authentication.adapter.external.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token é obrigatório.")
    val refreshToken: String,
)
