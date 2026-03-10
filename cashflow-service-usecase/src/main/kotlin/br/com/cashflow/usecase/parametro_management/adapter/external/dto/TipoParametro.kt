package br.com.cashflow.usecase.parametro_management.adapter.external.dto

enum class TipoParametro {
    TEXTO,
    DECIMAL,
    INTEIRO,
    ;

    fun toDbTipo(): String =
        when (this) {
            TEXTO -> "STRING"
            DECIMAL -> "DOUBLE"
            INTEIRO -> "INTEGER"
        }

    companion object {
        fun fromDbTipo(dbTipo: String): TipoParametro? =
            when (dbTipo.uppercase()) {
                "STRING" -> TEXTO
                "DOUBLE" -> DECIMAL
                "INTEGER" -> INTEIRO
                else -> null
            }
    }
}
