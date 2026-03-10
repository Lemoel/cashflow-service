package br.com.cashflow.usecase.tenant.model

data class TenantFilter(
    val nome: String? = null,
    val cnpj: String? = null,
    val active: Boolean? = null,
)
