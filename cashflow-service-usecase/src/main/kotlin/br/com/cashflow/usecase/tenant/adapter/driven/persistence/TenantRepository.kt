package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TenantRepository :
    JpaRepository<Tenant, UUID>,
    JpaSpecificationExecutor<Tenant> {
    fun findFiltered(
        filter: TenantFilter?,
        pageable: Pageable,
    ): Page<Tenant> {
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Order.asc("tradeName"))
        val p = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)
        return findAll(TenantSpecification.fromFilter(filter), p)
    }

    fun existsByCnpjAndIdNot(
        cnpj: String,
        id: UUID,
    ): Boolean

    fun existsByCnpj(cnpj: String): Boolean

    fun findByActiveTrueOrderByTradeNameAsc(): List<Tenant>

    @Query(value = "SELECT schema_name FROM core.tenants", nativeQuery = true)
    fun findAllSchemaNames(): List<String>

    @Query(
        value =
            "SELECT t.id AS tenantId, t.schema_name AS schemaName FROM core.user_tenant_map m " +
                "INNER JOIN core.tenants t ON t.id = m.tenant_id WHERE m.email = :email",
        nativeQuery = true,
    )
    fun findTenantSchemaByEmail(
        @Param("email") email: String,
    ): TenantSchemaInfoProjection?
}
