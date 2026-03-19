package br.com.cashflow.usecase.parametro.adapter.driven.persistence

import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object ParametroSpecification {
    fun fromFilter(filter: ParametroFilterModel?): Specification<Parametro> =
        Specification { root, _, cb ->
            if (filter == null) return@Specification cb.conjunction()
            val predicates = mutableListOf<Predicate>()
            filter.chave?.takeIf { it.isNotBlank() }?.let {
                predicates.add(cb.like(cb.lower(root.get("chave")), "%${it.lowercase()}%"))
            }
            filter.ativo?.let {
                predicates.add(cb.equal(root.get<Boolean>("ativo"), it))
            }
            if (predicates.isEmpty()) cb.conjunction() else cb.and(*predicates.toTypedArray())
        }
}
