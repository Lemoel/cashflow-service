package br.com.cashflow.usecase.bootstrap_management.port

import java.util.UUID

data class BootstrapResult(
    val tenantId: UUID,
    val tenantSchemaName: String,
    val adminEmail: String,
    val temporaryPassword: String,
    val congregationId: UUID,
)
