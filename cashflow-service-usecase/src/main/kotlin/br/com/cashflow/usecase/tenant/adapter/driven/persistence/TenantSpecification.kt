package br.com.cashflow.usecase.tenant.adapter.driven.persistence

import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantFilter
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object TenantSpecification {
    fun fromFilter(filter: TenantFilter?): Specification<Tenant> =
        Specification { root, _, cb ->
            if (filter == null) return@Specification cb.conjunction()
            val predicates = mutableListOf<Predicate>()
            filter.nome?.takeIf { it.isNotBlank() }?.let {
                predicates.add(cb.like(cb.lower(root.get<String>("tradeName")), "%${it.lowercase()}%"))
            }
            filter.cnpj?.takeIf { it.isNotBlank() }?.let {
                predicates.add(cb.equal(root.get<String>("cnpj"), it))
            }
            filter.active?.let {
                predicates.add(cb.equal(root.get<Boolean>("active"), it))
            }
            if (predicates.isEmpty()) cb.conjunction() else cb.and(*predicates.toTypedArray())
        }
}
