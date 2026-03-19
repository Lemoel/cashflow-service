package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import java.util.UUID

interface TenantSchemaInfoProjection {
    fun getTenantId(): UUID

    fun getSchemaName(): String
}
