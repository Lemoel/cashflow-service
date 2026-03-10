package br.com.cashflow.usecase.tenant_management.port

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantFilter
import br.com.cashflow.usecase.tenant.model.TenantPage
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantCreateRequest
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantUpdateRequest
import java.util.UUID

interface TenantManagementInputPort {
    fun create(request: TenantCreateRequest): Tenant

    fun update(
        id: UUID,
        request: TenantUpdateRequest,
    ): Tenant

    fun findById(id: UUID): Tenant?

    fun findAll(
        filter: TenantFilter?,
        page: Int,
        size: Int,
    ): TenantPage

    fun findActiveForList(): List<Tenant>

    fun delete(id: UUID)

    fun isCnpjAvailable(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean
}
