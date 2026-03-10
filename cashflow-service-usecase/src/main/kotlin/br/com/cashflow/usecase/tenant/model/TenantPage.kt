package br.com.cashflow.usecase.tenant.model

import br.com.cashflow.usecase.tenant.entity.Tenant

data class TenantPage(
    val items: List<Tenant>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)
