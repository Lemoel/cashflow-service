package br.com.cashflow.usecase.tenant.port

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantFilter
import br.com.cashflow.usecase.tenant.model.TenantPage
import br.com.cashflow.usecase.tenant.model.TenantSchemaInfo
import java.util.UUID

interface TenantOutputPort {
    fun save(tenant: Tenant): Tenant

    fun findById(id: UUID): Tenant?

    fun findAll(
        filter: TenantFilter?,
        page: Int,
        size: Int,
    ): TenantPage

    fun existsByCnpjExcludingId(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean

    fun findActiveOrderByTradeName(): List<Tenant>

    fun deleteById(id: UUID)

    fun findTenantSchemaByEmail(email: String): TenantSchemaInfo?

    fun findAllSchemaNames(): List<String>
}
