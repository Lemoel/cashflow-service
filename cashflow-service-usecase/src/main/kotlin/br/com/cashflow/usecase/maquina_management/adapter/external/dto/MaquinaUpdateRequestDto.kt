package br.com.cashflow.usecase.maquina_management.adapter.external.dto

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class MaquinaUpdateRequestDto(
    @field:NotNull(message = "É necessário selecionar uma congregação")
    val congregacaoId: UUID,

    @field:NotNull(message = "É necessário selecionar um banco")
    val bancoId: UUID,

    val departamentoId: UUID? = null,

    val ativo: Boolean = true,
)
