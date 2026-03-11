package br.com.cashflow.usecase.user_authentication.adapter.external.dto

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequestDto(
    @field:NotBlank(message = "Refresh token é obrigatório.")
    val refreshToken: String,
)
