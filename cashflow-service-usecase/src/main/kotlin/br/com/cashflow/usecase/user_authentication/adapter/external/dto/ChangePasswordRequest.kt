package br.com.cashflow.usecase.user_authentication.adapter.external.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @JsonProperty("senhaAtual")
    @field:NotBlank(message = "Senha atual é obrigatória.")
    val currentPassword: String,

    @JsonProperty("novaSenha")
    @field:NotBlank(message = "Nova senha é obrigatória.")
    @field:Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres.")
    val newPassword: String,
)
