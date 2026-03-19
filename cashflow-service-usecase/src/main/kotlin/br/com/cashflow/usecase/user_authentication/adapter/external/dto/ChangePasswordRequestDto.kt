package br.com.cashflow.usecase.user_authentication.adapter.external.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequestDto(
    @field:NotBlank(message = "Senha atual é obrigatória")
    val currentPassword: String,

    @field:NotBlank(message = "Nova senha é obrigatória")
    @field:Size(min = 6, message = "A nova senha deve ter pelo menos 6 caracteres")
    val newPassword: String,
)
