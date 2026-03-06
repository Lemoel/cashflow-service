package br.com.cashflow.usecase.congregation.port

data class CongregationFilter(
    val nome: String? = null,
    val cnpj: String? = null,
    val ativo: Boolean? = null,
)
