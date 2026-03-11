package br.com.cashflow.usecase.user_management.adapter.external.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID

data class UsuarioUpdateRequestDto(
    @field:NotBlank(message = "Nome é obrigatório.")
    @field:Size(max = 200)
    val nome: String,

    @field:NotBlank(message = "E-mail é obrigatório.")
    @field:Email(message = "Formato de e-mail inválido.")
    @field:Size(max = 255)
    val email: String,

    @field:Size(max = 11)
    @field:Pattern(regexp = "^[0-9]*$", message = "Telefone deve conter apenas dígitos.")
    val telefone: String? = null,

    @field:NotBlank(message = "Perfil é obrigatório.")
    @field:Size(max = 50)
    val perfil: String,

    @field:NotNull(message = "Congregação é obrigatória.")
    val congregacaoId: UUID,

    @field:NotNull(message = "Ativo é obrigatório.")
    val ativo: Boolean = true,
)
