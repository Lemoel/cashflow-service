package br.com.cashflow.usecase.user_authentication.adapter.external.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank(message = "E-mail é obrigatório.")
    @field:Email(message = "E-mail inválido.")
    val email: String,

    @JsonProperty("senha")
    @field:NotBlank(message = "Senha é obrigatória.")
    @field:Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres.")
    val password: String,
)
