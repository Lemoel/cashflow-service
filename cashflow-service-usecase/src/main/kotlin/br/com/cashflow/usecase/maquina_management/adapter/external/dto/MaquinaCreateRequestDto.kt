package br.com.cashflow.usecase.maquina_management.adapter.external.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class MaquinaCreateRequestDto(
    @field:NotBlank(message = "O ID da máquina é obrigatório")
    @field:Size(max = 20)
    val maquinaId: String,

    @field:NotNull(message = "É necessário selecionar uma congregação")
    val congregacaoId: UUID,

    @field:NotNull(message = "É necessário selecionar um banco")
    val bancoId: UUID,

    val departamentoId: UUID? = null,

    val ativo: Boolean = true,
)
