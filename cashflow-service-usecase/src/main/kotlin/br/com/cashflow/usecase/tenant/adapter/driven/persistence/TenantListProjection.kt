package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import java.util.UUID

interface TenantListProjection {
    fun getId(): UUID

    fun getTradeName(): String
}
