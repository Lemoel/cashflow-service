package br.com.cashflow.usecase.congregation.model

data class CongregationFilterModel(
    val nome: String? = null,
    val cnpj: String? = null,
    val ativo: Boolean? = null,
)
