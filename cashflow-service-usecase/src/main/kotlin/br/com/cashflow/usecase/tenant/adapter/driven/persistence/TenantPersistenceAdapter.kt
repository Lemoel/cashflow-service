package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantFilter
import br.com.cashflow.usecase.tenant.model.TenantIdName
import br.com.cashflow.usecase.tenant.model.TenantPage
import br.com.cashflow.usecase.tenant.model.TenantSchemaInfo
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TenantPersistenceAdapter(
    private val tenantRepository: TenantRepository,
) : TenantOutputPort {
    override fun save(tenant: Tenant): Tenant = tenantRepository.save(tenant)

    override fun findById(id: UUID): Tenant? = tenantRepository.findById(id).orElse(null)

    override fun findAll(
        filter: TenantFilter?,
        page: Int,
        size: Int,
    ): TenantPage {
        val pageable = PageRequest.of(page, size)
        val springPage = tenantRepository.findFiltered(filter, pageable)
        return TenantPage(
            items = springPage.content,
            total = springPage.totalElements,
            page = springPage.number,
            pageSize = springPage.size,
        )
    }

    override fun existsByCnpjExcludingId(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean =
        if (excludeId != null) {
            tenantRepository.existsByCnpjAndIdNot(cnpj, excludeId)
        } else {
            tenantRepository.existsByCnpj(cnpj)
        }

    override fun findActiveOrderByTradeName(): List<TenantIdName> =
        tenantRepository.findActiveIdAndTradeName().map { p ->
            TenantIdName(id = p.getId(), name = p.getTradeName())
        }

    override fun deleteById(id: UUID) {
        tenantRepository.deleteById(id)
    }

    override fun findTenantSchemaByEmail(email: String): TenantSchemaInfo? {
        val proj = tenantRepository.findTenantSchemaByEmail(email) ?: return null
        return TenantSchemaInfo(
            tenantId = proj.getTenantId(),
            schemaName = proj.getSchemaName(),
        )
    }

    override fun findAllSchemaNames(): List<String> = tenantRepository.findAllSchemaNames()
}
