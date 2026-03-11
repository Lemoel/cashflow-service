package br.com.cashflow.usecase.parametro_management.adapter.external.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ParametroUpdateRequestDto(
    @field:NotBlank(message = "A chave é obrigatória")
    @field:Size(max = 100)
    val chave: String = "",

    @field:NotBlank(message = "O valor é obrigatório")
    val valor: String = "",

    @field:NotNull(message = "O tipo é obrigatório")
    val tipo: EnumTipoParametro = EnumTipoParametro.TEXTO,

    val ativo: Boolean = true,
)
