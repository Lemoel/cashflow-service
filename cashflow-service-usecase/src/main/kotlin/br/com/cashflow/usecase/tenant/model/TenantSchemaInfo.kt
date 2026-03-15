package br.com.cashflow.usecase.tenant.model

import java.util.UUID

data class TenantSchemaInfo(
    val tenantId: UUID,
    val schemaName: String,
)
