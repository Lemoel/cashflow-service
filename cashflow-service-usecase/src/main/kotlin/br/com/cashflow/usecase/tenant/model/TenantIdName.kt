package br.com.cashflow.usecase.tenant.model

import java.util.UUID

data class TenantIdName(
    val id: UUID,
    val name: String,
)
