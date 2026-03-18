package br.com.cashflow.usecase.parametro_management.adapter.external.dto

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.EnumTipoParametro.Companion.fromDbTipo

data class ParametroResponseDto(
    val id: String,
    val chave: String,
    val valor: String,
    val tipo: EnumTipoParametro,
    val ativo: Boolean,
    val creationUserId: String,
    val createdAt: String?,
    val updatedAt: String?,
)

fun Parametro.toResponse(): ParametroResponseDto {
    val valorStr =
        when (tipo.uppercase()) {
            "STRING" -> valorTexto
            "INTEGER" -> valorInteiro?.toString()
            "DOUBLE" -> valorDecimal?.toString()
            else -> null
        }
    val tipoEnum = fromDbTipo(tipo) ?: EnumTipoParametro.TEXTO
    return ParametroResponseDto(
        id = id!!.toString(),
        chave = chave,
        valor = valorStr ?: "",
        tipo = tipoEnum,
        ativo = ativo,
        creationUserId = createdBy ?: "",
        createdAt = createdDate?.toString(),
        updatedAt = lastModifiedDate?.toString(),
    )
}

data class ParametroListResponse(
    val items: List<ParametroResponseDto>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)

data class ParametroChaveOption(
    val id: String,
    val nome: String,
)
