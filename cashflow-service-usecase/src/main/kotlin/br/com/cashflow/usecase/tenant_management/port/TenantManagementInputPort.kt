package br.com.cashflow.usecase.tenant_management.port

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantFilter
import br.com.cashflow.usecase.tenant.model.TenantIdName
import br.com.cashflow.usecase.tenant.model.TenantPage
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantCreateRequestDto
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantUpdateRequestDto
import java.util.UUID

interface TenantManagementInputPort {
    fun create(request: TenantCreateRequestDto): Tenant

    fun update(
        id: UUID,
        request: TenantUpdateRequestDto,
    ): Tenant

    fun findById(id: UUID): Tenant?

    fun findAll(
        filter: TenantFilter?,
        page: Int,
        size: Int,
    ): TenantPage

    fun findActiveForList(): List<TenantIdName>

    fun delete(id: UUID)

    fun isCnpjAvailable(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean
}
