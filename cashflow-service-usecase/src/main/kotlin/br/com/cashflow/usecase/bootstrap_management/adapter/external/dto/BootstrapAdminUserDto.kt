package br.com.cashflow.usecase.bootstrap_management.adapter.external.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class BootstrapAdminUserDto(
    @field:NotBlank(message = "Nome é obrigatório")
    @field:Size(max = 255)
    val nome: String,

    @field:NotBlank(message = "E-mail é obrigatório")
    @field:Email(message = "E-mail inválido")
    @field:Size(max = 255)
    val email: String,
)
