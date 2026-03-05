package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.port.TenantFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface TenantRepository : CrudRepository<Tenant, UUID>, TenantRepositoryCustom {

    fun existsByCnpjAndIdNot(cnpj: String, id: UUID): Boolean
    fun existsByCnpj(cnpj: String): Boolean
    fun findByActiveTrueOrderByTradeNameAsc(): List<Tenant>
}

interface TenantRepositoryCustom {
    fun findFiltered(filter: TenantFilter?, pageable: Pageable): Page<Tenant>
}
