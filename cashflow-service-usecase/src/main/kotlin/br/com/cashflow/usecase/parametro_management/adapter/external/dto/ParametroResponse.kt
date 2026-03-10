package br.com.cashflow.usecase.parametro_management.adapter.external.dto

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.TipoParametro.Companion.fromDbTipo

data class ParametroResponse(
    val id: String,
    val chave: String,
    val valor: String,
    val tipo: TipoParametro,
    val ativo: Boolean,
    val creationUserId: String,
    val createdAt: String?,
)

fun Parametro.toResponse(): ParametroResponse {
    val valorStr =
        when (tipo.uppercase()) {
            "STRING" -> valorTexto
            "INTEGER" -> valorInteiro?.toString()
            "DOUBLE" -> valorDecimal?.toString()
            else -> null
        }
    val tipoEnum = fromDbTipo(tipo) ?: TipoParametro.TEXTO
    return ParametroResponse(
        id = id!!.toString(),
        chave = chave,
        valor = valorStr ?: "",
        tipo = tipoEnum,
        ativo = ativo,
        creationUserId = creationUserId,
        createdAt = createdAt?.toString(),
    )
}

data class ParametroListResponse(
    val items: List<ParametroResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)

data class ParametroChaveOption(
    val id: String,
    val nome: String,
)
