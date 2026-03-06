package br.com.cashflow.usecase.department_management.adapter.external.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class DepartmentCreateRequest(
    @field:NotBlank(message = "Nome do departamento é obrigatório")
    @field:Size(max = 255)
    val nome: String,

    val ativo: Boolean = true,
)
