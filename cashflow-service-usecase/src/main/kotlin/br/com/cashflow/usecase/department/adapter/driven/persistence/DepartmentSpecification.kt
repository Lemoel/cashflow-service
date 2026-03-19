package br.com.cashflow.usecase.department.adapter.driven.persistence

import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.model.DepartmentFilter
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object DepartmentSpecification {
    fun fromFilter(filter: DepartmentFilter?): Specification<Department> =
        Specification { root, _, cb ->

            if (filter == null) return@Specification cb.conjunction()

            val predicates = mutableListOf<Predicate>()

            filter.tenantId?.let {
                predicates.add(cb.equal(root.get<java.util.UUID>("tenantId"), it))
            }

            filter.nome?.takeIf { it.isNotBlank() }?.let {
                predicates.add(cb.like(cb.lower(root.get("nome")), "%${it.lowercase()}%"))
            }

            filter.ativo?.let {
                predicates.add(cb.equal(root.get<Boolean>("ativo"), it))
            }

            if (predicates.isEmpty()) cb.conjunction() else cb.and(*predicates.toTypedArray())
        }
}
