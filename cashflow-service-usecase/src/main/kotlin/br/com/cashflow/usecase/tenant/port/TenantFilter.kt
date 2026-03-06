package br.com.cashflow.usecase.tenant.port

data class TenantFilter(
    val nome: String? = null,
    val cnpj: String? = null,
    val active: Boolean? = null,
)
