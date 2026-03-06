package br.com.cashflow.usecase.congregation_management.adapter.external.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CongregationUpdateRequest(
    val setorialId: UUID? = null,
    @field:NotBlank(message = "O nome é obrigatório")
    @field:Size(max = 255)
    val nome: String = "",
    @field:Size(max = 18)
    val cnpj: String? = null,
    @field:NotBlank(message = "O logradouro é obrigatório")
    @field:Size(max = 255)
    val logradouro: String = "",
    @field:NotBlank(message = "O bairro é obrigatório")
    @field:Size(max = 255)
    val bairro: String = "",
    @field:NotBlank(message = "O número é obrigatório")
    @field:Size(max = 20)
    val numero: String = "",
    @field:NotBlank(message = "A cidade é obrigatória")
    @field:Size(max = 255)
    val cidade: String = "",
    @field:NotBlank(message = "A UF é obrigatória")
    @field:Size(min = 2, max = 2)
    val uf: String = "",
    @field:NotBlank(message = "O CEP é obrigatório")
    @field:Size(max = 9)
    val cep: String = "",
    @field:Size(max = 255)
    val email: String? = null,
    @field:Size(max = 20)
    val telefone: String? = null,
    val ativo: Boolean = true,
)
