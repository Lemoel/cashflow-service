package br.com.cashflow.usecase.congregation.adapter.driven.persistence

import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilterModel
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object CongregationSpecification {
    fun fromFilter(filter: CongregationFilterModel?): Specification<Congregation> =
        Specification { root, _, cb ->
            if (filter == null) return@Specification cb.conjunction()
            val predicates = mutableListOf<Predicate>()

            filter.nome?.takeIf { it.isNotBlank() }?.let {
                predicates.add(cb.like(cb.upper(root.get<String>("nome")), "%${it.uppercase()}%"))
            }

            filter.cnpj?.takeIf { it.isNotBlank() }?.let {
                predicates.add(cb.equal(root.get<String>("cnpj"), it))
            }

            filter.ativo?.let {
                predicates.add(cb.equal(root.get<Boolean>("ativo"), it))
            }

            if (predicates.isEmpty()) cb.conjunction() else cb.and(*predicates.toTypedArray())
        }
}
