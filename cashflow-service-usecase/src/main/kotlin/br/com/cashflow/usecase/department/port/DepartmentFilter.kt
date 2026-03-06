package br.com.cashflow.usecase.department.port

import java.util.UUID

data class DepartmentFilter(
    val tenantId: UUID? = null,
    val nome: String? = null,
    val ativo: Boolean? = null,
)
